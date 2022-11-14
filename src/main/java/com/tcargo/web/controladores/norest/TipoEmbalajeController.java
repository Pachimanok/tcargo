package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoEmbalaje;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoEmbalajeModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoEmbalajeService;
import com.tcargo.web.servicios.UsuarioService;
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
@RequestMapping("/tipoEmbalaje")
public class TipoEmbalajeController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;
    private final TipoEmbalajeService tipoEmbalajeService;
    private final UsuarioService usuarioService;

    public TipoEmbalajeController(MessageSource messages, PaisService paisService, TipoEmbalajeService tipoEmbalajeService, UsuarioService usuarioService) {
        super("tipoEmbalaje-list", "tipoEmbalaje-form");
        this.messages = messages;
        this.paisService = paisService;
        this.tipoEmbalajeService = tipoEmbalajeService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPOEMBALAJE_LABEL) TipoEmbalajeModel tipoEmbalaje, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    tipoEmbalaje.setIdPais((u.getPais().getId()));
                }
                tipoEmbalajeService.guardar(tipoEmbalaje);
                return "redirect:/tipoEmbalaje/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.embalaje.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.embalaje.back.error.inesperado", null, locale) + e.getMessage());
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPOEMBALAJE_LABEL) TipoEmbalajeModel tipoEmbalaje, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");

        try {
            tipoEmbalajeService.eliminar(tipoEmbalaje.getId());
            return "redirect:/tipoEmbalaje/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("tipo.embalaje.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoEmbalajeModel tipoEmbalaje = new TipoEmbalajeModel();

        if (id != null) {
            tipoEmbalaje = tipoEmbalajeService.buscar(id);
        }

        modelo.addObject(TIPOEMBALAJE_LABEL, tipoEmbalaje);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<TipoEmbalaje> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = tipoEmbalajeService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = tipoEmbalajeService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = tipoEmbalajeService.listarActivos(paginable, excel != null);
            } else {
                page = tipoEmbalajeService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/tipoEmbalaje/listado");
        modelo.addObject(TIPOEMBALAJE_LABEL, new TipoEmbalajeModel());
        return modelo;
    }

}
