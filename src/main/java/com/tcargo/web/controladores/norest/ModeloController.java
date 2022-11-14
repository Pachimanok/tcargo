package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Marca;
import com.tcargo.web.entidades.Modelo;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ModeloModel;
import com.tcargo.web.servicios.MarcaService;
import com.tcargo.web.servicios.ModeloService;
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
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/modelo")
public class ModeloController extends Controlador {

    private final MarcaService marcaService;
    private final MessageSource messages;
    private final ModeloService modeloService;
    private final PaisService paisService;
    private final UsuarioService usuarioService;

    @Autowired
    public ModeloController(MarcaService marcaService, MessageSource messages, ModeloService modeloService, PaisService paisService, UsuarioService usuarioService) {
        super("modelo-list", "modelo-form");
        this.marcaService = marcaService;
        this.messages = messages;
        this.modeloService = modeloService;
        this.paisService = paisService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(MODELO_LABEL) ModeloModel modeloMarca, BindingResult resultado, ModelMap model, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(model, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL && u.getPais() != null) {
                    modeloMarca.setIdPais(u.getPais().getId());
                }
                modeloService.guardar(modeloMarca);
                return "redirect:/modelo/listado";

            }
        } catch (WebException e) {
            model.addAttribute(ERROR, messages.getMessage("modelo.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("modelo.back.error.inesperado", null, locale) + e.getMessage());
            log.error(ERROR_INESPERADO, e);
        }
        List<Marca> marcas = marcaService.listarActivos();
        model.addAttribute(MARCAS_LABEL, marcas);
        model.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(MODELO_LABEL) ModeloModel modeloMarca, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            modeloService.eliminar(modeloMarca.getId());
            return "redirect:/modelo/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("modelo.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView model = new ModelAndView(vistaFormulario);
        ModeloModel modeloMarca = new ModeloModel();

        if (id != null) {
            modeloMarca = modeloService.buscar(id);
        }

        List<Marca> marcas = marcaService.listarActivos();

        model.addObject(MARCAS_LABEL, marcas);
        model.addObject(MODELO_LABEL, modeloMarca);
        model.addObject(ACCION_LABEL, accion);
        model.addObject(PAISES_LABEL, paisService.listarActivos());
        return model;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView model = new ModelAndView(vistaListado);
        ordenar(paginable, model);
        Page<Modelo> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());

        if (q == null || q.isEmpty()) {
            page = modeloService.listarActivos(paginable, excel != null);
        } else {
            page = modeloService.listarActivos(paginable, q, excel != null);
            model.addObject(QUERY_LABEL, q);
        }

        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = modeloService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = modeloService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                model.addObject(QUERY_LABEL, q);
            }
        }

        model.addObject(PAGE_LABEL, page);

        model.addObject(URL_LABEL, "/modelo/listado");
        model.addObject(MODELO_LABEL, new ModeloModel());
        return model;
    }

}
