package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.Provincia;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ProvinciaModel;
import com.tcargo.web.servicios.PaisService;
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
@RequestMapping("/provincia")
public class ProvinciaController extends Controlador {

    @Autowired
    private ProvinciaService provinciaService;

    @Autowired
    private PaisService paisService;

    public ProvinciaController() {
        super("provincia-list", "provincia-form");
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(PROVINCIA_LABEL) ProvinciaModel provincia, BindingResult resultado, ModelMap modelo) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                provinciaService.guardar(provincia);
                return "redirect:/provincia/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, "Ocurrió un error al intentar modificar la provincia. " + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, "Ocurrió un error inesperado al intentar modificar la provincia.");
            log.error(ERROR_INESPERADO, e);
        }
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(PROVINCIA_LABEL) ProvinciaModel provincia, ModelMap model) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            provinciaService.eliminar(provincia.getId());
            return "redirect:/provincia/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, "Ocurrió un error inesperado al intentar eliminar la provincia.");
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        ProvinciaModel provincia = new ProvinciaModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            provincia = provinciaService.buscar(id);
        }

        List<Pais> paises = paisService.listarActivos();
        modelo.addObject(PAISES_LABEL, paises);
        modelo.addObject(PROVINCIA_LABEL, provincia);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<Provincia> page = null;
        if (q == null || q.isEmpty()) {
            page = provinciaService.listarActivos(paginable);
        } else {
            page = provinciaService.listarActivos(paginable, q);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/provincia/listado");
        modelo.addObject(PROVINCIA_LABEL, new ProvinciaModel());
        return modelo;
    }

}
