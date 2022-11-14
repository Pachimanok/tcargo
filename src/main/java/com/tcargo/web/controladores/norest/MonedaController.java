package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Moneda;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.MonedaModel;
import com.tcargo.web.servicios.MonedaService;
import com.tcargo.web.servicios.PaisService;
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
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/moneda")
public class MonedaController extends Controlador {

    private final PaisService paisService;
    private final MessageSource messages;
    private final MonedaService monedaService;
    private final UsuarioService usuarioService;

    @Autowired
    public MonedaController(MonedaService monedaService, MessageSource messages, PaisService paisService, UsuarioService usuarioService) {
        super("moneda-list", "moneda-form");
        this.paisService = paisService;
        this.messages = messages;
        this.monedaService = monedaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(MONEDA_LABEL) MonedaModel moneda, BindingResult resultado, ModelMap model, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(model, resultado);
            } else {
                if (u.getPais() != null) {
                    moneda.setIdPais(u.getPais().getId());
                }
                monedaService.guardar(moneda);
                return "redirect:/moneda/listado";

            }
        } catch (WebException e) {
            model.addAttribute(ERROR, messages.getMessage("moneda.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("moneda.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }

        model.addAttribute(PAISES_LABEL, paisService.listarActivos());
        model.addAttribute(MONEDA_LABEL, moneda);
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(MODELO_LABEL) MonedaModel monedaMarca, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            monedaService.eliminar(monedaMarca.getId());
            return "redirect:/moneda/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("moneda.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView model = new ModelAndView(vistaFormulario);
        MonedaModel monedaMarca = new MonedaModel();

        if (id != null) {
            monedaMarca = monedaService.buscar(id);
        }

        model.addObject(MONEDA_LABEL, monedaMarca);
        model.addObject(ACCION_LABEL, accion);
        model.addObject(PAISES_LABEL, paisService.listarActivos());
        return model;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView model = new ModelAndView(vistaListado);
        ordenar(paginable, model);
        Page<Moneda> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (q == null || q.isEmpty()) {
            page = monedaService.listarActivos(paginable, excel != null);
        } else {
            page = monedaService.listarActivos(paginable, q, excel != null);
            model.addObject(QUERY_LABEL, q);
        }

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = monedaService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = monedaService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                model.addObject(QUERY_LABEL, q);
            }
        }

        model.addObject(PAGE_LABEL, page);

        model.addObject(URL_LABEL, "/moneda/listado");
        model.addObject(MODELO_LABEL, new MonedaModel());
        return model;
    }

}
