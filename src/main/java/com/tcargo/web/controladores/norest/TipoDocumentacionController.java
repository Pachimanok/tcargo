package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoDocumentacion;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoDocumentacionModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoDocumentacionService;
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
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/tipoDocumentacion")
public class TipoDocumentacionController extends Controlador {

    private final TipoDocumentacionService tipoDocumentacionService;
    private final PaisService paisService;
    private final UsuarioService usuarioService;
    private final MessageSource messajes;

    @Autowired
    public TipoDocumentacionController(TipoDocumentacionService tipoDocumentacionService, PaisService paisService,
                                       UsuarioService usuarioService, MessageSource messajes) {
        super("tipoDocumentacion-List", "tipoDocumentacion-form");
        this.tipoDocumentacionService = tipoDocumentacionService;
        this.paisService = paisService;
        this.usuarioService = usuarioService;
        this.messajes = messajes;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPODOCUMENTACION_LABEL) TipoDocumentacionModel tipoDocumentacion, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    tipoDocumentacion.setIdPais(u.getPais().getId());
                }
                tipoDocumentacionService.guardar(tipoDocumentacion);
                return "redirect:/tipoDocumentacion/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.documentacion.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.documentacion.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPODOCUMENTACION_LABEL) TipoDocumentacionModel tipoDocumentacion, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoDocumentacionService.eliminar(tipoDocumentacion.getId());
            return "redirect:/tipoDocumentacion/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("tipo.documentacion.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoDocumentacionModel tipoDocumentacion = new TipoDocumentacionModel();

        if (id != null) {
            tipoDocumentacion = tipoDocumentacionService.buscar(id);
        }
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        modelo.addObject(TIPODOCUMENTACION_LABEL, tipoDocumentacion);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<TipoDocumentacion> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = tipoDocumentacionService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = tipoDocumentacionService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = tipoDocumentacionService.listarActivos(paginable, excel != null);
            } else {
                page = tipoDocumentacionService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/tipoDocumentacion/listado");
        modelo.addObject(TIPODOCUMENTACION_LABEL, new TipoDocumentacionModel());
        return modelo;
    }

}
