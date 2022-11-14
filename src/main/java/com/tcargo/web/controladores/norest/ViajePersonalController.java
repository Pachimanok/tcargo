package com.tcargo.web.controladores.norest;

import com.tcargo.web.convertidores.ViajePersonalConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaParaViajePropioModel;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.modelos.ViajePersonalModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@RequestMapping("/viajepersonal")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_TRANSPORTADOR','ROLE_AMBAS', 'ROLE_DADOR_CARGA','ROLE_ADMINISTRADOR', 'ROLE_ADMINISTRADOR_LOCAL', 'ROLE_CHOFER')")
public class ViajePersonalController extends Controlador {

    private final ChoferService choferService;
    private final CoincidenciaService coincidenciaService;
    private final RemolqueService remolqueService;
    private final TipoCargaService tipoCargaService;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;
    private final ViajePersonalConverter viajePersonalConverter;
    private final ViajePersonalService viajePersonalService;
    private final MessageSource messajes;

    @Autowired
    public ViajePersonalController(ChoferService choferService, CoincidenciaService coincidenciaService, RemolqueService remolqueService,
                                   TipoCargaService tipoCargaService, TransportadorService transportadorService, UsuarioService usuarioService,
                                   VehiculoService vehiculoService, ViajePersonalConverter viajePersonalConverter, ViajePersonalService viajePersonalService,
                                   MessageSource messajes) {
        super("viajePersonal-list", "viajePersonal-form");
        this.choferService = choferService;
        this.coincidenciaService = coincidenciaService;
        this.remolqueService = remolqueService;
        this.tipoCargaService = tipoCargaService;
        this.transportadorService = transportadorService;
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
        this.viajePersonalConverter = viajePersonalConverter;
        this.viajePersonalService = viajePersonalService;
        this.messajes = messajes;
    }

    @PostMapping("/actualizarestado")
    public String actualizarestado(Pageable paginable, @RequestParam("id") String id, @RequestParam("estado") EstadoViaje estado, RedirectAttributes flash, Locale locale) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        try {
            viajePersonalService.actualizarEstado(id, estado);
        } catch (WebException e) {
            flash.addFlashAttribute(ERROR, messajes.getMessage("text.registro.strong", null, locale) + e.getMessage());
            return "redirect:/viajepersonal/listado";

        }
        flash.addFlashAttribute("info", "OK! Estado actualizado con éxito.");
        return "redirect:/viajepersonal/listado";
    }

    @GetMapping("/formulario")
    public ModelAndView form(HttpSession session, @RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        ViajePersonalModel viajePersonalModel = new ViajePersonalModel();
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(user.getId());
        List<CoincidenciaParaViajePropioModel> matchs = new ArrayList<>();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            ViajePersonal vp = viajePersonalService.porIdEntidad(id);
            viajePersonalModel = viajePersonalConverter.entidadToModelo(vp);
            matchs.addAll(coincidenciaService.armarModelosParaViaje(vp.getCoincidencias()));

        }

        matchs.addAll(coincidenciaService.armarModelosParaViajePropiosLibres(transportador));

        modelo.addObject(VIAJE_LABEL, viajePersonalModel);
        modelo.addObject(COINCIDENCIAS_LABEL, matchs);
        modelo.addObject(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(CHOFERES_LABEL, choferService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(ACCION_LABEL, accion);


        return modelo;
    }

    @PostMapping("/eliminar")
    public String eliminar(@Valid @ModelAttribute(VIAJE_LABEL) ViajePersonalModel viaje, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            viajePersonalService.eliminar(viaje.getId());
            return "redirect:/provincia/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("viaje.personal.back.error.inesperado", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/listado")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView list(HttpSession session, Pageable paginable, @ModelAttribute BusquedaViajeModel busquedaViaje) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<ViajePersonalModel> page = null;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        List<TipoCarga> tipoDeCargas = null;
        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {

//        	page = viajePersonalService.buscarPorCriterios(paginable, busquedaViaje, null);
            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());

        } else if (u.getRol() == Rol.ADMINISTRADOR || u.getRol() == Rol.SIN_ROL) {

//        	page = viajePersonalService.buscarPorCriterios(paginable, busquedaViaje, null);
            tipoDeCargas = tipoCargaService.listarActivos();

        } else if (u.getRol() == Rol.TRANSPORTADOR) {
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
            page = viajePersonalService.buscarPorCriteriosPropios(paginable, busquedaViaje, t);
            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
            modelo.addObject(CHOFERES_LABEL, t.getChoferes());
            modelo.addObject(VEHICULOS_LABEL, t.getVehiculos());
            modelo.addObject(REMOLQUES_LABEL, t.getRemolques());

        } else if (u.getRol() == Rol.AMBAS) {
            String rolActual = session.getAttribute("rolActual").toString();
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());

            if (rolActual.equals(TRANSPORTADOR_LABEL)) {

                page = viajePersonalService.buscarPorCriteriosPropios(paginable, busquedaViaje, t);
                tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
                modelo.addObject(CHOFERES_LABEL, t.getChoferes());
                modelo.addObject(VEHICULOS_LABEL, t.getVehiculos());
                modelo.addObject(REMOLQUES_LABEL, t.getRemolques());

            }
        } else if (u.getRol() == Rol.CHOFER) {
            Chofer chofer = choferService.buscarPorUsuario(u.getId());
            Transportador t = chofer.getTransportador();
            busquedaViaje.setChofer(chofer.getId());
            page = viajePersonalService.buscarPorCriteriosPropios(paginable, busquedaViaje, t);
            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(ESTADOS_LABEL, EstadoViaje.getEstados());
        modelo.addObject(TIPOCARGAS_LABEL, tipoDeCargas);
        modelo.addObject(URL_LABEL, "/viajepersonal/listado");
        modelo.addObject(VIAJE_LABEL, new ViajeModel());
        return modelo;
    }

}
