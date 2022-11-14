package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.UbicacionService;
import com.tcargo.web.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.tcargo.web.utiles.Textos.*;

import java.util.Locale;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS')")
@RequestMapping("/ubicacion")
public class UbicacionController extends Controlador {

    private final UbicacionService ubicacionService;
    private final UsuarioService usuarioService;
    private final PaisService paisService;
    private final MessageSource messajes;


    @Autowired
    public UbicacionController(UbicacionService ubicacionService, UsuarioService usuarioService, PaisService paisService, MessageSource mesagges) {
        super("ubicacion-list", "ubicacion-form");
        this.ubicacionService = ubicacionService;
        this.usuarioService = usuarioService;
        this.paisService = paisService;
        this.messajes =  mesagges;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(UBICACION_LABEL) UbicacionModel ubicacion, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    ubicacion.setIdPais(u.getPais().getId());
                }
                ubicacion.setIsMasterPoint(true);
                ubicacionService.guardar(ubicacion);
                return "redirect:/ubicacion/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messajes.getMessage("ubicacion.back.error.al.guardar", null, locale)  + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messajes.getMessage("ubicacion.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(UBICACION_LABEL) UbicacionModel ubicacion, ModelMap model, Locale  locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            ubicacionService.eliminar(ubicacion.getId());
            return "redirect:/ubicacion/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("ubicacion.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        UbicacionModel ubicacion = new UbicacionModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            ubicacion = ubicacionService.buscar(id);
        }

        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        modelo.addObject(UBICACION_LABEL, ubicacion);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        Page<Ubicacion> page;

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = ubicacionService.listarActivosPorPais(paginable, u.getPais().getId());
            } else {
                page = ubicacionService.listarActivosPorPais(paginable, q, u.getPais().getId());
                modelo.addObject(QUERY_LABEL, q);
            }

        } else {
            if (q == null || q.isEmpty()) {
                page = ubicacionService.listarActivos(paginable);
            } else {
                page = ubicacionService.listarActivos(paginable, q);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/ubicacion/listado");
        modelo.addObject(UBICACION_LABEL, new UbicacionModel());
        return modelo;
    }

}
