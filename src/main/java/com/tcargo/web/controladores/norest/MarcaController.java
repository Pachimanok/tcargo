package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Marca;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.MarcaModel;
import com.tcargo.web.servicios.MarcaService;
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

import javax.validation.Valid;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/marca")
public class MarcaController extends Controlador {

    private final MarcaService marcaService;
    private final MessageSource messages;

    @Autowired
    public MarcaController(MarcaService marcaService, MessageSource messages) {
        super("marca-list", "marca-form");
        this.marcaService = marcaService;
        this.messages = messages;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute(MARCA_LABEL) MarcaModel marca, BindingResult resultado, ModelMap modelo, Locale locale) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                marcaService.guardar(marca);
                return "redirect:/marca/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("marca.bback.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("marca.bback.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(MARCA_LABEL) MarcaModel marca, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            marcaService.eliminar(marca.getId());
            return "redirect:/marca/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("marca.bback.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        MarcaModel marca = new MarcaModel();

        if (id != null) {
            marca = marcaService.buscar(id);
        }

        modelo.addObject(MARCA_LABEL, marca);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<Marca> page;
        if (q == null || q.isEmpty()) {
            page = marcaService.listarActivos(paginable, excel != null);
        } else {
            page = marcaService.listarActivos(paginable, q, excel != null);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/marca/listado");
        modelo.addObject(MARCA_LABEL, new MarcaModel());
        return modelo;
    }

}
