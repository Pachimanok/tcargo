package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.modelos.busqueda.BusquedaHistorialModel;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_TRANSPORTADOR','ROLE_AMBAS','ROLE_DADOR_CARGA', 'ROLE_INVITADO')")
@RequestMapping("/contraoferta")
public class  ContraOfertaController extends Controlador {

    private final ContraOfertaService contraOfertaService;
    private final TransportadorService transportadorService;
    private final PedidoService pedidoService;
    private final VehiculoService vehiculoService;
    private final RemolqueService remolqueService;
    private final UsuarioService usuarioService;
    private final TipoRequisitoOfertaService tipoRequisitoOfertaService;
    private final FirebaseService firebaseService;
    private final MonedaService monedaService;
    private final MessageSource messages;

    @Autowired
    public ContraOfertaController(ContraOfertaService contraOfertaService, TransportadorService transportadorService, PedidoService pedidoService, VehiculoService vehiculoService, MonedaService monedaService, RemolqueService remolqueService, UsuarioService usuarioService, TipoRequisitoOfertaService tipoRequisitoOfertaService, FirebaseService firebaseService, MessageSource messages) {
        super("contraoferta-list", "contraoferta-form");
        this.contraOfertaService = contraOfertaService;
        this.transportadorService = transportadorService;
        this.pedidoService = pedidoService;
        this.vehiculoService = vehiculoService;
        this.remolqueService = remolqueService;
        this.usuarioService = usuarioService;
        this.tipoRequisitoOfertaService = tipoRequisitoOfertaService;
        this.firebaseService = firebaseService;
        this.monedaService = monedaService;
        this.messages = messages;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(CONTRAOFERTA_LABEL) ContraOfertaModel contraOferta, BindingResult resultado, ModelMap modelo, @RequestParam(value = "idUser") String userId, @RequestParam(value = "idPedidos", required = false) List<Long> idPedidos, Locale locale) {
        final UsuarioModel usuario = new UsuarioModel();
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                Usuario u = usuarioService.buscar(userId);
                String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);

                if (u.getRol() == Rol.TRANSPORTADOR) {
                    contraOferta.setIdTransportador(transportadorService.buscarPorIdUsuario(u.getId()).getId());
                    contraOferta.setIdCreador(u.getId());
                    if (contraOferta.getIdRemolque().equals("")) {
                        contraOferta.setIdRemolque(null);
                    }
                    if (contraOferta.getIdVehiculo().equals("")) {
                        contraOferta.setIdVehiculo(null);
                    }
                    usuario.setId(contraOferta.getIdDador());
                } else if (u.getRol() == Rol.DADOR_CARGA) {
                    contraOferta.setIdCreador(u.getId());
                    usuario.setId(transportadorService.buscarEntidad(contraOferta.getIdTransportador()).getUsuario().getId());
                } else if (u.getRol() == Rol.AMBAS) {
                    if (rolActual.equals(TRANSPORTADOR_LABEL)) {
                        contraOferta.setIdCreador(u.getId());
                        Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
                        contraOferta.setIdTransportador(t.getId());
                        usuario.setId(contraOferta.getIdDador());
                    } else {
                        contraOferta.setIdCreador(u.getId());
                        usuario.setId(transportadorService.buscarPorIdUsuario(contraOferta.getIdTransportador()).getUsuario().getId());
                    }
                }

                ContraOferta co = contraOfertaService.guardar(contraOferta);
                new Thread(() -> firebaseService.contraofertaNueva(usuario.getId(), contraOferta)).start();
                if (idPedidos != null && idPedidos.size() > 1) {
                    for (int i = 1; i < idPedidos.size(); i++) {
                        contraOfertaService.copiarYguardar(co, idPedidos.get(i));
                    }
                }
                modelo.addAttribute("alertInfo", messages.getMessage("contra.oferta.back.succes", null, locale));
                return "redirect:/";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("contra.oferta.back.ocurrio.error", null, locale) + e.getMessage());
            modelo.addAttribute("error", messages.getMessage("contra.oferta.back.error.solicitud", null, locale));
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("contra.oferta.back.ocurrio.error", null, locale));
            modelo.addAttribute("error", messages.getMessage("contra.oferta.back.error.solicitud", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        return "redirect:/";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam(value = "contraofertaid") String contraOfertaid, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            contraOfertaService.eliminar(contraOfertaid);
            return "redirect:/contraoferta/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("contra.oferta.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(HttpSession session, @RequestParam(required = false) String id, @RequestParam(required = false) String accion, @RequestParam(value = "idcontraOfertaDesastimar", required = false) String idcontraOfertaDesastimar, @RequestParam(value = "idPedidos", required = false) List<Long> idPedidos) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        ContraOfertaModel contraOferta = new ContraOfertaModel();
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        Pedido p = null;
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            contraOferta = contraOfertaService.buscar(id);
            modelo.addObject("entidad", pedidoService.buscarPorId(Long.valueOf(contraOferta.getIdPedido())));
        }

        if (u.getRol() == Rol.TRANSPORTADOR || u.getRol() == Rol.AMBAS) {
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
            if (idcontraOfertaDesastimar != null) {
                modelo.addObject("contraOfertaAdesestimar", contraOfertaService.buscar(idcontraOfertaDesastimar));
            }
            modelo.addObject(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(t.getId()));
            modelo.addObject(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(t.getId()));
        }

        if (idPedidos != null && !idPedidos.isEmpty()) {
            p = pedidoService.buscarPorId(idPedidos.get(0));
            modelo.addObject("entidad", p);
            if (idPedidos.size() > 1) {
                modelo.addObject("idPedidos", idPedidos);
            }
        }

        modelo.addObject(MONEDAS_LABEL, monedaService.listarActivosPorPais(idPais));
        modelo.addObject(TIPOREQUISITOSOFERTAS_LABEL, p != null ? p.getTipoRequisitos() : null);
        modelo.addObject(CONTRAOFERTA_LABEL, contraOferta);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/admin/listado")
    @PreAuthorize("hasAnyRole('ROLE_INVITADO','ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL')")
    public ModelAndView listarParaAdmin(@PageableDefault(sort = "c.modificacion", direction = Direction.DESC, size = 20) Pageable paginable, @RequestParam(value = "idPedido", required = false) Long idPedido, @RequestParam(value = "valor", required = false) Double valor, @RequestParam(value = "vehiculo", required = false) String vehiculo, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView("contraoferta-list-admin");
        ordenar(paginable, modelo);
        Page<ContraOferta> page = contraOfertaService.busqueda(paginable, idPedido, valor, vehiculo, q != null ? q.trim() : q, excel != null);

        modelo.addObject("idPedido", idPedido);
        modelo.addObject("valor", valor);
        modelo.addObject("vehiculo", vehiculo);
        modelo.addObject(QUERY_LABEL, q);
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/contraoferta/admin/listado");
        modelo.addObject(CONTRAOFERTA_LABEL, new ContraOfertaModel());
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listarParaTransportador(HttpSession session, Pageable paginable, @RequestParam(value = "idPedido", required = false) Long pedidoId, @RequestParam(value = "rol") Rol rol) {
        ModelAndView modelo = new ModelAndView("contraoferta-list");
        ordenar(paginable, modelo);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        List<ContraOfertaModel> cOfertas;
        Page<ContraOfertaModel> page = null;

        if (rol == Rol.TRANSPORTADOR || rol == Rol.AMBAS) {
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
            Pedido p = pedidoService.buscarPorId(pedidoId);
            cOfertas = contraOfertaService.listarPorPedidoList(p);
            for (ContraOfertaModel c : cOfertas) {
                for (String tro : c.getIdRequisitosContraOferta()) {
                    String nombre = tipoRequisitoOfertaService.buscar(tro).getNombre();
                    c.getRequisitosNombres().add(nombre);
                }
            }
            List<ContraOfertaModel> index = new ArrayList<>();
            for (ContraOfertaModel cOferta : cOfertas) {
                if (!cOferta.getIdTransportador().equals(t.getId())) {
                    index.add(cOferta);
                }
            }

            if (!index.isEmpty()) {
                cOfertas.removeAll(index);
            }
            page = new PageImpl<>(cOfertas, paginable, cOfertas.size());
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/contraoferta/dador/listado");
        modelo.addObject(CONTRAOFERTA_LABEL, new ContraOfertaModel());
        return modelo;
    }

    @GetMapping("/dador/listado")
    public ModelAndView listarParaDador(HttpSession session, Pageable paginable, @RequestParam(value = "idPedido", required = false) Long pedidoId, @RequestParam(value = "rol") Rol rol) {
        ModelAndView modelo = new ModelAndView("contraoferta-list-dador");
        ordenar(paginable, modelo);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        List<ContraOfertaModel> cOfertas;
        Page<ContraOfertaModel> page = null;

        if (rol == Rol.DADOR_CARGA || rol == Rol.AMBAS) {
            Pedido p = pedidoService.buscarPorId(pedidoId);
            cOfertas = contraOfertaService.listarPorPedidoList(p);
            for (ContraOfertaModel c : cOfertas) {
                for (String tro : c.getIdRequisitosContraOferta()) {
                    String nombre = tipoRequisitoOfertaService.buscar(tro).getNombre();
                    c.getRequisitosNombres().add(nombre);
                }
            }

            List<ContraOfertaModel> index = new ArrayList<>();
            for (ContraOfertaModel cOferta : cOfertas) {
                if (cOferta.getIdCreador().equals(u.getId())) {
                    index.add(cOferta);
                }
            }

            if (!index.isEmpty()) {
                cOfertas.removeAll(index);
            }

            modelo.addObject(TIPOREQUISITOSOFERTAS_LABEL, p.getTipoRequisitos());
            page = new PageImpl<>(cOfertas, paginable, cOfertas.size());
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/contraoferta/dador/listado");
        modelo.addObject(CONTRAOFERTA_LABEL, new ContraOfertaModel());
        return modelo;
    }

    @GetMapping("/historial")
    public ModelAndView historial(@PageableDefault(value = 20, sort = {"modificacion"}) Pageable pageable, @ModelAttribute BusquedaHistorialModel busqueda, HttpSession session) {
        ModelAndView mav = new ModelAndView("contraoferta-historial");
        ordenar(pageable, mav);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);

        mav.addObject(URL_LABEL, "/contraoferta/historial" + busqueda.toString());
        mav.addObject(BUSCADOR_LABEL, busqueda);
        mav.addObject(ESTADOS_LABEL, EstadoContraOferta.values());

        if (TRANSPORTADOR_LABEL.equals(rolActual)) {
            mav.addObject(PAGE_LABEL, contraOfertaService.buscarPorTransportador(pageable, transportadorService.buscarPorIdUsuario(usuario.getId()).getId(), busqueda));
            return mav;
        }

        if (DADORDECARGA_LABEL.equals(rolActual)) {
            mav.addObject(PAGE_LABEL, contraOfertaService.buscarPorDador(pageable, usuario.getId(), busqueda));
            return mav;
        }

        return new ModelAndView(new RedirectView("/dashboard"));
    }

}
