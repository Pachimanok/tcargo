package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoCargaModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoCargaService;
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
@RequestMapping("/tipoCarga")
public class TipoCargaController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;
    private final TipoCargaService tipoCargaService;
    private final UsuarioService usuarioService;

    @Autowired
    public TipoCargaController(MessageSource messages, PaisService paisService, TipoCargaService tipoCargaService, UsuarioService usuarioService) {
        super("tipoCarga-list", "tipoCarga-form");
        this.messages = messages;
        this.paisService = paisService;
        this.tipoCargaService = tipoCargaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPOCARGA_LABEL) TipoCargaModel tipoCarga, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    tipoCarga.setIdPais(u.getPais().getId());
                }
                tipoCargaService.guardar(tipoCarga);
                return "redirect:/tipoCarga/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.carga.controller.error.al.guardar", null, locale)  + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("pais.back.en.service.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }

        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPOCARGA_LABEL) TipoCargaModel tipoCarga, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoCargaService.eliminar(tipoCarga.getId());
            return "redirect:/tipoCarga/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("tipo.carga.controller.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoCargaModel tipoCarga = new TipoCargaModel();

        if (id != null) {
            tipoCarga = tipoCargaService.buscar(id);
        }

        modelo.addObject(TIPOCARGA_LABEL, tipoCarga);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<TipoCarga> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = tipoCargaService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = tipoCargaService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = tipoCargaService.listarActivos(paginable, excel != null);
            } else {
                page = tipoCargaService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/tipoCarga/listado");
        modelo.addObject(TIPOCARGA_LABEL, new TipoCargaModel());
        return modelo;
    }

}
