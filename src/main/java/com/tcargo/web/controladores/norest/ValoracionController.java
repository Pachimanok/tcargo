
package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Valoracion;
import com.tcargo.web.enumeraciones.EstadoValoracion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.servicios.ValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO', 'ROLE_TRANSPORTADOR', 'ROLE_AMBAS', 'ROLE_DADOR_CARGA')")
@RequestMapping("/valoracion")
public class ValoracionController extends Controlador {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private MessageSource messajes;

    public ValoracionController() {
        super("valoracion-list", "valoracion-form");
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(VALORACION) ValoracionModel valoracion, BindingResult resultado, ModelMap model, Locale locale) {
        try {
            if (resultado.hasErrors()) {
                error(model, resultado);
            } else {
                valoracionService.guardar(valoracion);
                return "redirect:/";
            }
        } catch (WebException e) {
            model.addAttribute(ERROR, messajes.getMessage("valoracion.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("valoracion.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        model.addAttribute(VALORACION, valoracion);
        return vistaFormulario;
    }

    @GetMapping("/formulario/{id}")
    public ModelAndView formulario(@PathVariable String id, @RequestParam(required = false) String accion) {
        ModelAndView model = new ModelAndView(vistaFormulario);
        ValoracionModel valoracion = new ValoracionModel();

        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            valoracion = valoracionService.buscarModel(id);
        }

        model.addObject(VALORACION, valoracion);
        model.addObject(ACCION_LABEL, accion);

        return model;
    }

    @GetMapping("/ver/{id}")
    public ModelAndView detalle(@PathVariable String id) {
        ModelAndView model = new ModelAndView("valoracion-detalle");
        ValoracionModel valoracion = new ValoracionModel();

        if (id != null) {
            valoracion = valoracionService.buscarModel(id);
            valoracion.setEstadoValoracion(EstadoValoracion.FINALIZADA_REVISADA);
            try {
                valoracionService.guardar(valoracion);
            } catch (WebException e) {
                e.printStackTrace();
            }
        }

        model.addObject(VALORACION, valoracion);

        return model;
    }

    @GetMapping("/listado/{id}")
    public ModelAndView listado(@PageableDefault(sort = {"modificacion"}) Pageable page, @PathVariable String id, @RequestParam(value = "propio", required = false) boolean propio) {
        ModelAndView model = new ModelAndView(vistaListado);
        Page<Valoracion> valoraciones;

        if (propio) {
            log.info("::::::::::::::ID DEL USUARIO EN DENTRO  DEL  IF DONDE TIRA NULL POINTER::::::::::::::");
            log.info(id);
            valoraciones = valoracionService.buscarPorIdReceptor(page, id);
            model.addObject("promedio", valoracionService.sacarPromedio(id));
            model.addObject("propio", propio);
        } else {
            valoraciones = valoracionService.buscarPorIdCreador(page, id);
        }

        model.addObject(PAGE_LABEL, valoraciones);
        return model;
    }

}
