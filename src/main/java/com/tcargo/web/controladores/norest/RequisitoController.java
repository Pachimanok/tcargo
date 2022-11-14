package com.tcargo.web.controladores.norest;

import com.tcargo.web.convertidores.RequisitoConverter;
import com.tcargo.web.entidades.Requisito;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RequisitoModel;
import com.tcargo.web.servicios.RequisitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS')")
@RequestMapping("/requisito")
public class RequisitoController extends Controlador {

    private final RequisitoService requisitoService;
    private final RequisitoConverter requisitoConverter;

    @Autowired
    public RequisitoController(RequisitoService requisitoService, RequisitoConverter requisitoConverter) {
        super("requisito-list", "requisito-form");
        this.requisitoService = requisitoService;
        this.requisitoConverter = requisitoConverter;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute(REQUISITO_LABEL) RequisitoModel requisito, BindingResult resultado,
                          ModelMap modelo) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                requisitoService.guardar(requisito);
                return "redirect:/requisito/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, "Ocurrió un error al intentar modificar el requisito. " + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, "Ocurrió un error inesperado al intentar modificar el requisito.");
            log.error(ERROR_INESPERADO, e);
        }
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(REQUISITO_LABEL) RequisitoModel requisito, ModelMap model) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            requisitoService.eliminar(requisito.getId());
            return "redirect:/requisito/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, "Ocurrió un error inesperado al intentar eliminar el requisito.");
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        RequisitoModel requisito = new RequisitoModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            requisito = requisitoConverter.entidadToModelo(requisitoService.buscar(id));
        }
        modelo.addObject(REQUISITO_LABEL, requisito);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(Pageable paginable, @RequestParam(required = false) String q) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<Requisito> page;
        if (q == null || q.isEmpty()) {
            page = requisitoService.listarActivos(paginable);
        } else {
            page = requisitoService.listarActivos(paginable, q);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/requisito/listado");
        modelo.addObject(REQUISITO_LABEL, new RequisitoModel());
        return modelo;
    }

}
