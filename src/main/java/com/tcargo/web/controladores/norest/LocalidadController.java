package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Localidad;
import com.tcargo.web.entidades.Provincia;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.LocalidadModel;
import com.tcargo.web.servicios.LocalidadService;
import com.tcargo.web.servicios.ProvinciaService;
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS')")
@RequestMapping("/localidad")
public class LocalidadController extends Controlador {

    @Autowired
    private LocalidadService localidadService;

    @Autowired
    private ProvinciaService provinciaService;

    public LocalidadController() {
        super("localidad-list", "localidad-form");
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(LOCALIDAD_LABEL) LocalidadModel localidad, BindingResult resultado, ModelMap modelo) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                localidadService.guardar(localidad);
                return "redirect:/localidad/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, "Ocurrió un error al intentar modificar la localidad. " + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, "Ocurrió un error inesperado al intentar modificar la localidad.");
            log.error(ERROR_INESPERADO, e);
        }
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(LOCALIDAD_LABEL) LocalidadModel localidad, ModelMap model) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            localidadService.eliminar(localidad.getId());
            return "redirect:/localidad/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, "Ocurrió un error inesperado al intentar eliminar la localidad.");
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        LocalidadModel localidad = new LocalidadModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            localidad = localidadService.buscar(id);
        }

        List<Provincia> provincias = provinciaService.listarActivos();
        modelo.addObject(PROVINCIAS_LABEL, provincias);
        modelo.addObject(LOCALIDAD_LABEL, localidad);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<Localidad> page = null;
        if (q == null || q.isEmpty()) {
            page = localidadService.listarActivos(paginable);
        } else {
            page = localidadService.listarActivos(paginable, q);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/localidad/listado");
        modelo.addObject(LOCALIDAD_LABEL, new LocalidadModel());
        return modelo;
    }

}
