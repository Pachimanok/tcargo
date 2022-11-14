package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PaisModel;
import com.tcargo.web.servicios.PaisService;
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
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/pais")
public class PaisController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;

    @Autowired
    public PaisController(MessageSource messages, PaisService paisService) {
        super("pais-list", "pais-form");
        this.messages = messages;
        this.paisService = paisService;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute(PAIS_LABEL) PaisModel pais, BindingResult resultado, ModelMap modelo, Locale locale) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                paisService.guardar(pais);
                return "redirect:/pais/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("pais.back.en.service.error.inesperado", null, locale) + " " + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("pais.back.en.service.error.modificacion", null, locale));
            log.error(ERROR_INESPERADO, e);
        }

        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(PAIS_LABEL) PaisModel pais, ModelMap model, Locale locale) {
        try {
            paisService.eliminar(pais.getId());
            return "redirect:/pais/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("pais.back.en.service.error.eliminacion", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        PaisModel pais = new PaisModel();

        if (id != null) {
            pais = paisService.buscar(id);
        }
        modelo.addObject(PAIS_LABEL, pais);
        modelo.addObject(ACCION_LABEL, accion);

        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<Pais> page;
        if (q == null || q.isEmpty()) {
            page = paisService.listarActivos(paginable, excel != null);
        } else {
            page = paisService.listarActivos(paginable, q, excel != null);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/pais/listado");
        modelo.addObject(PAIS_LABEL, new PaisModel());

        return modelo;
    }

}
