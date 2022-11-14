package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.entidades.Viaje;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@RequestMapping("/viaje")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_TRANSPORTADOR','ROLE_AMBAS', 'ROLE_DADOR_CARGA','ROLE_ADMINISTRADOR', 'ROLE_ADMINISTRADOR_LOCAL', 'ROLE_INVITADO')")
public class ViajeController extends Controlador {

    private final ModeloService modelosService;
    private final RemolqueService remolqueService;
    private final TipoCargaService tipoCargaService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TipoVehiculoService tipoVehiculoService;
    private final TransportadorService transportadorService;
    private final UbicacionService ubicacionService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;
    private final ViajeService viajeService;
    private final MessageSource messages;

    @Autowired
    public ViajeController(ModeloService modeloService, RemolqueService remolqueService, TipoCargaService tipoCargaService,
                           TipoRemolqueService tipoRemolqueService, TipoVehiculoService tipoVehiculoService, TransportadorService transportadorService,
                           UbicacionService ubicacionService, UsuarioService usuarioService, VehiculoService vehiculoService, ViajeService viajeService, MessageSource messages) {
        super("viaje-list", "viaje-form");
        this.modelosService = modeloService;
        this.remolqueService = remolqueService;
        this.tipoCargaService = tipoCargaService;
        this.tipoRemolqueService = tipoRemolqueService;
        this.tipoVehiculoService = tipoVehiculoService;
        this.transportadorService = transportadorService;
        this.ubicacionService = ubicacionService;
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
        this.viajeService = viajeService;
        this.messages = messages;
    }

    @GetMapping("/listado")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listar(HttpSession session, @PageableDefault(size = 20, sort = "pc.finalizacion", direction = Sort.Direction.DESC) Pageable paginable, @ModelAttribute BusquedaViajeModel busquedaViaje) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<Viaje> page = null;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        List<TipoCarga> tipoDeCargas = null;
        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
        } else if (u.getRol() == Rol.ADMINISTRADOR || u.getRol() == Rol.SIN_ROL || u.getRol() == Rol.INVITADO) {
            page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
            tipoDeCargas = tipoCargaService.listarActivos();
        } else if (u.getRol() == Rol.TRANSPORTADOR) {
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
            paginable = PageRequest.of(paginable.getPageNumber(), paginable.getPageSize(), Sort.by(Sort.Direction.DESC, "v.partida_carga_negativa"));
            if(u.getPais() != null) {
	            page = viajeService.buscarPorCriteriosPropios(paginable, busquedaViaje, t);
	            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
            }else {
            	page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
                tipoDeCargas = tipoCargaService.listarActivos();
            }
            modelo.addObject(CHOFERES_LABEL, t.getChoferes());
            modelo.addObject(VEHICULOS_LABEL, t.getVehiculos());
            modelo.addObject(REMOLQUES_LABEL, t.getRemolques());
        } else if (u.getRol() == Rol.DADOR_CARGA) {
            page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
            if(u.getPais() != null) {
	            tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
	            modelo.addObject(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
	            modelo.addObject(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
	            modelo.addObject(MODELOS_LABEL, modelosService.listarActivosPorPais(u.getPais().getId()));
            }else {
            	tipoDeCargas = tipoCargaService.listarActivos();
            }
        } else if (u.getRol() == Rol.AMBAS) {
            String rolActual = session.getAttribute(ROL_ACTUAL_LABEL).toString();
            Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
            
            if (rolActual.equals(TRANSPORTADOR_LABEL)) {
                paginable = PageRequest.of(paginable.getPageNumber(), paginable.getPageSize(), Sort.by(Sort.Direction.DESC, "v.partida_carga_negativa"));
                if(u.getPais() != null) {
	                page = viajeService.buscarPorCriteriosPropios(paginable, busquedaViaje, t);
	                tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
                }else {
                	page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
                    tipoDeCargas = tipoCargaService.listarActivos();
                }
                modelo.addObject(CHOFERES_LABEL, t.getChoferes());
                modelo.addObject(VEHICULOS_LABEL, t.getVehiculos());
                modelo.addObject(REMOLQUES_LABEL, t.getRemolques());
            } else if (rolActual.equals(DADORDECARGA_LABEL)) {
            	if(u.getPais() != null) {
	                page = viajeService.buscarPorCriterios(paginable, busquedaViaje, t);
	                tipoDeCargas = tipoCargaService.activosPorPais(u.getPais().getId());
            	}else {
            		page = viajeService.buscarPorCriterios(paginable, busquedaViaje, null);
            		tipoDeCargas = tipoCargaService.listarActivos();
            	}
            }
        }

        Map<String, Object> mapa = new HashMap<>();
        if (page.getContent() != null && !page.getContent().isEmpty()) {
            mapa = viajeService.armarMapa(page.getContent());
        }
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject("mapa", mapa);
        modelo.addObject(TIPOCARGAS_LABEL, tipoDeCargas);
        modelo.addObject(URL_LABEL, "/viaje/listado");
        modelo.addObject(VIAJE_LABEL, new ViajeModel());
        modelo.addObject("busquedaViajeModel", busquedaViaje);
        return modelo;
    }


    @GetMapping("/formulario")
    public ModelAndView form(HttpSession session, @RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        ViajeModel viajeModel = new ViajeModel();
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(user.getId());

        if (id != null) {
            viajeModel = viajeService.porIdModel(id);
            modelo.addObject("ubicacionInicial", ubicacionService.buscar(viajeModel.getIdUbicacionInicial()));
            modelo.addObject("ubicacionFinal", ubicacionService.buscar(viajeModel.getIdUbicacionFinal()));
        }

        modelo.addObject(VIAJE_LABEL, viajeModel);
        modelo.addObject(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addObject(ACCION_LABEL, accion);

        return modelo;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(VIAJE_LABEL) ViajeModel viaje, BindingResult result,
                          ModelMap modelo, Locale locale) {
        try {
            if (result.hasErrors()) {
                error(modelo, result);
            } else {
                viajeService.guardar(viaje);
                return "redirect:/viaje/listado";
            }
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("viaje.back.end.controller.inesperado", null, locale) + e.getMessage());
            log.error(ERROR_INESPERADO, e);
        }
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(user.getId());
        modelo.addAttribute(VIAJES_LABEL, viajeService.listarActivos());
        modelo.addAttribute(VIAJE_LABEL, viaje);
        modelo.addAttribute(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(transportador.getId()));
        modelo.addAttribute(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(transportador.getId()));

        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@Valid @ModelAttribute(VIAJE_LABEL) ViajeModel viaje, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            viajeService.eliminar(viaje.getId());
            return "redirect:/provincia/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("viaje.back.end.controller.inesperado", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/ver/{id}")
    public ModelAndView ver(Pageable pageable, @PathVariable String id) {
        ModelAndView model = new ModelAndView(vistaListado);

        List<Viaje> viajes = new ArrayList<>();
        viajes.add(viajeService.porIdEntidad(id));
        Page<Viaje> page = new PageImpl<>(viajes, pageable, 1L);
        model.addObject(PAGE_LABEL, page);

        Map<String, Object> mapa = new HashMap<>();
        if (!page.getContent().isEmpty()) {
            mapa = viajeService.armarMapa(page.getContent());
        }
        model.addObject("mapa", mapa);
        model.addObject("detalle", true);
        return model;
    }

    @GetMapping("/oportunidades/{id}")
    public ModelAndView verOportunidadesCercanas(@PathVariable String id, Locale locale) {
        ModelAndView mav = new ModelAndView("oportunidades-cercanas");
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Viaje viaje = viajeService.porIdEntidad(id);
        mav.addObject(VIAJE_LABEL, viaje);
        mav.addObject("partida", sdf.format(viaje.getPartidaCargaNegativa()));
        mav.addObject("llegada", sdf.format(viaje.getLlegadaCargaNegativa()));
        mav.addObject(PRODUCTO_LABEL, messages.getMessage("viaje.back.end.controller.producto", null, locale));
        mav.addObject("desde", messages.getMessage("viaje.back.end.controller.desde", null, locale));
        mav.addObject("hasta", messages.getMessage("viaje.back.end.controller.hasta", null, locale));
        mav.addObject("fechaCarga", messages.getMessage("viaje.back.end.controller.fecha.carga", null, locale));
        mav.addObject("fechaDescarga", messages.getMessage("viaje.back.end.controller.fecha.descarga", null, locale));
        mav.addObject("verMasDetalles", messages.getMessage("viaje.back.end.controller.ver.mas", null, locale));
        return mav;
    }

}
