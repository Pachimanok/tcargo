package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoRequisitoOfertaModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoRequisitoOfertaService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/tipoRequisitoOferta")
public class TipoRequisitoOfertaController extends Controlador {

    private final TipoRequisitoOfertaService tipoRequisitoOfertaService;
    private final PaisService paisService;
    private final UsuarioService usuarioService;
    private final MessageSource messajes;

    @Autowired
    public TipoRequisitoOfertaController(TipoRequisitoOfertaService tipoRequisitoOfertaService, PaisService paisService,
                                         UsuarioService usuarioService, MessageSource messajes) {
        super("tipoRequisitoOferta-List", "tipoRequisitoOferta-form");
        this.tipoRequisitoOfertaService = tipoRequisitoOfertaService;
        this.paisService = paisService;
        this.usuarioService = usuarioService;
        this.messajes = messajes;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPOREQUISITOOFERTA_LABEL) TipoRequisitoOfertaModel tipoRequisitoOferta, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    tipoRequisitoOferta.setIdPais(u.getPais().getId());
                }
                tipoRequisitoOfertaService.guardar(tipoRequisitoOferta);
                return "redirect:/tipoRequisitoOferta/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.requisito.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.requisito.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        List<TipoDeViaje> tp = Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL, TipoDeViaje.AMBOS);
        modelo.addAttribute("tipoDeViaje", tp);
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPOREQUISITOOFERTA_LABEL) TipoRequisitoOfertaModel tipoRequisitoOferta, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoRequisitoOfertaService.eliminar(tipoRequisitoOferta.getId());
            return "redirect:/tipoRequisitoOferta/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("tipo.requisito.back.error.eliminar", null, locale));
            List<TipoDeViaje> tp = Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL, TipoDeViaje.AMBOS);
            model.addAttribute("tipoDeViaje", tp);
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoRequisitoOfertaModel tipoRequisitoOferta = new TipoRequisitoOfertaModel();

        if (id != null) {
            tipoRequisitoOferta = tipoRequisitoOfertaService.buscar(id);
        }
        List<TipoDeViaje> tp = Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL, TipoDeViaje.AMBOS);
        modelo.addObject("tipoDeViaje", tp);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        modelo.addObject(TIPOREQUISITOOFERTA_LABEL, tipoRequisitoOferta);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<TipoRequisitoOferta> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = tipoRequisitoOfertaService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = tipoRequisitoOfertaService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = tipoRequisitoOfertaService.listarActivos(paginable, excel != null);
            } else {
                page = tipoRequisitoOfertaService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/tipoRequisitoOferta/listado");
        modelo.addObject(TIPOREQUISITOOFERTA_LABEL, new TipoRequisitoOfertaModel());
        return modelo;
    }

}
