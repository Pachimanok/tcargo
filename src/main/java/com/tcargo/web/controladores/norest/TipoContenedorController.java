package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoContenedor;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoContenedorModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoContenedorService;
import com.tcargo.web.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/tipoContenedor")
public class TipoContenedorController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;
    private final TipoContenedorService tipoContenedorService;
    private final UsuarioService usuarioService;

    @Autowired
    public TipoContenedorController(MessageSource messages, PaisService paisService, TipoContenedorService tipoContenedorService, UsuarioService usuarioService) {
        super("tipoContenedor-list", "tipoContenedor-form");
        this.messages = messages;
        this.paisService = paisService;
        this.tipoContenedorService = tipoContenedorService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPOCONTENEDOR_LABEL) TipoContenedorModel tipoContenedor, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    tipoContenedor.setIdPais(u.getPais().getId());
                }
                tipoContenedorService.guardar(tipoContenedor);
                return "redirect:/tipoContenedor/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.contenedor.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.contenedor.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPOCONTENEDOR_LABEL) TipoContenedorModel tipoContenedor, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoContenedorService.eliminar(tipoContenedor.getId());
            return "redirect:/tipoContenedor/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("tipo.contenedor.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoContenedorModel tipoContenedor = new TipoContenedorModel();

        if (id != null) {
            tipoContenedor = tipoContenedorService.buscar(id);
        }
        modelo.addObject(TIPOCONTENEDOR_LABEL, tipoContenedor);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        if (paginable.getSort().isEmpty()) {
            paginable = PageRequest.of(paginable.getPageNumber(), paginable.getPageSize(), Sort.by("caracteristicas").ascending());
            modelo.addObject("sort_field", "caracteristicas");
            modelo.addObject("sort_dir", "ASC");
        } else {
            ordenar(paginable, modelo);
        }
        Page<TipoContenedor> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = tipoContenedorService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = tipoContenedorService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = tipoContenedorService.listarActivos(paginable, excel != null);
            } else {
                page = tipoContenedorService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/tipoContenedor/listado");
        modelo.addObject(TIPOCONTENEDOR_LABEL, new TipoContenedorModel());
        return modelo;
    }

}
