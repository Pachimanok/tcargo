package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.VehiculoModel;
import com.tcargo.web.modelos.busqueda.BusquedaVehiculoModel;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_TRANSPORTADOR', 'ROLE_AMBAS', 'ADMINISTRADOR_LOCAL', 'ROLE_INVITADO')")
@RequestMapping("/vehiculo")
public class VehiculoController extends Controlador {

    private final MarcaService marcaService;
    private final MessageSource messages;
    private final ModeloService modeloService;
    private final TipoCargaService tipoCargaService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TipoVehiculoService tipoVehiculoService;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;

    @Autowired
    public VehiculoController(MarcaService marcaService, MessageSource messages, ModeloService modeloService, TipoCargaService tipoCargaService, TipoRemolqueService tipoRemolqueService,
                              TipoVehiculoService tipoVehiculoService, TransportadorService transportadorService, UsuarioService usuarioService, VehiculoService vehiculoService) {
        super("vehiculo-list", "vehiculo-form");
        this.marcaService = marcaService;
        this.messages = messages;
        this.modeloService = modeloService;
        this.tipoCargaService = tipoCargaService;
        this.tipoRemolqueService = tipoRemolqueService;
        this.tipoVehiculoService = tipoVehiculoService;
        this.transportadorService = transportadorService;
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(VEHICULO_LABEL) VehiculoModel vehiculo, Pageable paginable, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                Vehiculo v = vehiculoService.guardar(vehiculo);
                transportadorService.addVehiculo(v);
                return "redirect:/documentacion/formulario/vehiculo/" + v.getId();
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("vehiculo.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("vehiculo.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        String transportador = transportadorService.buscar(vehiculo.getId()).getId();
        modelo.addAttribute(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addAttribute(USUARIO_LABEL, u);
        modelo.addAttribute(MODELOS_LABEL, modeloService.listarActivosPorPais(paginable, u.getPais().getId(), true));
        modelo.addAttribute(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
        modelo.addAttribute(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivos());
        modelo.addAttribute(TRANSPORTADORES_LABEL, transportador);
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(VEHICULO_LABEL) VehiculoModel vehiculo, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            vehiculoService.eliminar(vehiculo.getId());
            return "redirect:/vehiculo/listado/" + vehiculo.getIdTransportador();
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("vehiculo.back.error.al.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(HttpSession session, @RequestParam(required = false, value = "id") String id,
                                   @RequestParam(required = false) String accion,
                                   @RequestParam(required = false, value = "transportadorid") String transportadorId) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        VehiculoModel vehiculo = new VehiculoModel();
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            vehiculo = vehiculoService.buscar(id);
        }
        if (transportadorId != null) {
            vehiculo.setIdTransportador(transportadorId);
        }
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject(MODELOS_LABEL, modeloService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
        modelo.addObject(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
        modelo.addObject(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivos());
        modelo.addObject(USUARIO_LABEL, u);
        modelo.addObject(VEHICULO_LABEL, vehiculo);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping({ "/listado", "/listado/{idTransportador}" })
    public ModelAndView listar(@PageableDefault(size = 20) Pageable pageable, @PathVariable(required = false) String idTransportador,
                               @ModelAttribute BusquedaVehiculoModel busqueda, HttpSession session) {
        ModelAndView mav = new ModelAndView(vistaListado);
        String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);

        if (pageable.getSort().isEmpty()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "ma.nombre", "m.nombre"));
            mav.addObject("sort_field", "ma.nombre,m.nombre");
            mav.addObject("sort_dir", "ASC");
        }
        ordenar(pageable, mav);

        Page<Vehiculo> page;
        if (idTransportador != null && !idTransportador.isEmpty()
                && !rolActual.equals(ADMINISTRADOR_LABEL) && !rolActual.equals(ADMINISTRADORLOCAL_LABEL)) { // CUANDO ES TRANSPORTADOR
            page = vehiculoService.buscarPorCriterios(pageable, busqueda, idTransportador);
            mav.addObject(MODELOS_LABEL, modeloService.listarActivosPorPais(idPais));
            mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPais(idPais));
        } else { // CUANDO ES ADMIN
            if (idTransportador != null && !idTransportador.isEmpty()) {
                page = vehiculoService.buscarPorCriterios(pageable, busqueda, idTransportador);
            } else {
                page = vehiculoService.buscarPorCriterios(pageable, busqueda, null);
            }
            mav.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivosModel());
            mav.addObject(MODELOS_LABEL, modeloService.listarActivos());
            mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.listarActivos());
        }

        mav.addObject(URL_LABEL, "/vehiculo/listado/" + idTransportador + busqueda.toString());
        mav.addObject(BUSCADOR_LABEL, busqueda);
        mav.addObject(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivosModel());
        mav.addObject(MARCAS_LABEL, marcaService.listarActivos());
        mav.addObject(PAGE_LABEL, page);
        mav.addObject(ID_TRANSPORTADOR_LABEL, idTransportador);
        return mav;
    }

}
