package com.tcargo.web.controladores.norest;

import com.tcargo.web.convertidores.CoincidenciaConverter;
import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.PedidoModel;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@RequestMapping("/match")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_TRANSPORTADOR','ROLE_AMBAS','ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL', 'ROLE_INVITADO')")
public class CoincidenciaController extends Controlador {

    private final ChoferService choferService;
    private final CoincidenciaService coincidenciaService;
    private final CoincidenciaConverter coincidenciaConverter;
    private final EmailService emailService;
    private final FirebaseService firebaseService;
    private final MessageSource messages;
    private final PaisService paisService;
    private final PedidoService pedidoService;
    private final RemolqueService remolqueService;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;

    @Autowired
    public CoincidenciaController(ChoferService choferService, CoincidenciaService coincidenciaService, CoincidenciaConverter coincidenciaConverter,
                                  EmailService emailService, FirebaseService firebaseService, MessageSource messages,
                                  PaisService paisService, PedidoService pedidoService, RemolqueService remolqueService,
                                  TransportadorService transportadorService, UsuarioService usuarioService, VehiculoService vehiculoService) {
        super("match-list", "match-form");
        this.choferService = choferService;
        this.coincidenciaService = coincidenciaService;
        this.coincidenciaConverter = coincidenciaConverter;
        this.emailService = emailService;
        this.firebaseService = firebaseService;
        this.messages = messages;
        this.paisService = paisService;
        this.pedidoService = pedidoService;
        this.remolqueService = remolqueService;
        this.transportadorService = transportadorService;
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, @PageableDefault(size = 20, sort = "modificacion") Pageable pageable, @RequestParam(required = false) String q, @RequestParam(required = false, value = "transportadorId") String idTransportador, @RequestParam(value = "excel", required = false) String excel) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        ModelAndView model = new ModelAndView(vistaListado);
        Page<Coincidencia> page = null;
        ordenar(pageable, model);
        if (idTransportador != null && (u.getRol() == Rol.ADMINISTRADOR || u.getRol() == Rol.ADMINISTRADOR_LOCAL || u.getRol() == Rol.INVITADO)) {
            page = obtenerPaginaAdminOTransportador(pageable, q, idTransportador, excel);
        } else {
            if (u.getRol() == Rol.TRANSPORTADOR || u.getRol() == Rol.AMBAS) {
                Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
                page = obtenerPaginaAdminOTransportador(pageable, q, t.getId(), excel);
            } else if (u.getRol() == Rol.ADMINISTRADOR || u.getRol() == Rol.INVITADO) {
                page = coincidenciaService.listarActivos(pageable, q, excel != null);
            } else if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                page = coincidenciaService.buscarPorPais(pageable, u.getPais().getId(), q, excel != null);
            }
        }

        model.addObject(QUERY_LABEL, q);
        model.addObject(PAGE_LABEL, page);
        model.addObject(URL_LABEL, "/match/listado");
        model.addObject(COINCIDENCIA_LABEL, new CoincidenciaModel());

        return model;
    }

    private Page<Coincidencia> obtenerPaginaAdminOTransportador(Pageable pageable, String q, String idTransportador, String excel) {
        if (excel != null) {
            return coincidenciaService.buscarParaExcel(pageable, idTransportador, q);
        } else {
            return coincidenciaService.buscarActivosPorIdTransportador(pageable, idTransportador, q);
        }
    }

    @GetMapping("/form")
    public ModelAndView formulario(HttpSession session, @RequestParam("idPedido") Long id, @RequestParam(name = "error", required = false) String error) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        PedidoModel pedido = pedidoService.pedidoModelParaMatchForm(id);
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(user.getId());

        if (error != null) {
            modelo.addObject(ERROR, error);
        }

        modelo.addObject(PEDIDO_LABEL, pedido);
        modelo.addObject(CHOFERES_LABEL, choferService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(transportador.getId()));

        return modelo;
    }

    @PostMapping("/nuevo/{idPedido}/{idViaje}")
    public RedirectView crearNuevoMatch(HttpSession session, @PathVariable("idPedido") Long idPedido, @PathVariable("idViaje") String idViaje, Locale locale) {
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Pais pais = paisService.buscarParaMatch(user.getId());
        CoincidenciaModel match;

        try {
            match = coincidenciaService.crearModelo(idPedido);
        } catch (WebException e) {
            log.error(messages.getMessage("match.back.error.al.crear", null, locale) + e.getMessage());
            return new RedirectView("/match/form?idPedido=" + idPedido + "&error=" + e.getMessage());
        }

        match.setIdTransportador(transportadorService.buscarPorIdUsuario(user.getId()).getId());
        match.setIdViaje(idViaje);

        try {
            Coincidencia matchPersistido = coincidenciaService.guardar(match, pais);
            pedidoService.asignarTransportadorTrue(idPedido);
            emailService.match(matchPersistido.getTransportador().getUsuario().getMail(), matchPersistido.getTransportador().getUsuario().getNombre(), matchPersistido.getPedido().getId());
            emailService.match(matchPersistido.getPedido().getDador().getMail(), matchPersistido.getPedido().getDador().getNombre(), matchPersistido.getPedido().getId());
            new Thread(() -> firebaseService.match(matchPersistido)).start();
        } catch (WebException e) {
            log.error(e.getMessage());
        }

        return new RedirectView("/match/listado");
    }

    @GetMapping("/conformidad/form")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView conformidadFormulario(@RequestParam("idPedido") Long id, @RequestParam(name = "error", required = false) String error) {
        ModelAndView modelo = new ModelAndView("conformidad-form");

        CoincidenciaModel match = coincidenciaConverter.entidadToModelo(coincidenciaService.buscarPorPedidoEntidad(id));
        if (error != null) {
            modelo.addObject(ERROR, error);
        }
        if (match.getViaje() == null || match.getViaje().getEstadoViaje() != EstadoViaje.FINALIZADO) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }

        modelo.addObject(COINCIDENCIA_LABEL, match);

        return modelo;
    }

}
