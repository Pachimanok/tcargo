package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoVehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoVehiculoModel;
import com.tcargo.web.servicios.TipoVehiculoService;
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
@RequestMapping("/tipoVehiculo")
public class TipoVehiculoController extends Controlador {

    @Autowired
    private TipoVehiculoService tipoVehiculoService;
    
    @Autowired
    private MessageSource messajes;

    public TipoVehiculoController() {
        super("tipoVehiculo-list", "tipoVehiculo-form");
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TIPOVEHICULO_LABEL) TipoVehiculoModel tipoVehiculo, BindingResult resultado, ModelMap modelo,  Locale locale) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                tipoVehiculoService.guardar(tipoVehiculo);
                return "redirect:/tipoVehiculo/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.vehiculo.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messajes.getMessage("tipo.vehiculo.back.error.inesperador", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPOVEHICULO_LABEL) TipoVehiculoModel tipoVehiculo, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoVehiculoService.eliminar(tipoVehiculo.getId());
            return "redirect:/tipoVehiculo/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("tipo.vehiculo.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoVehiculoModel tipoVehiculo = new TipoVehiculoModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            tipoVehiculo = tipoVehiculoService.buscar(id);
        }
        modelo.addObject(TIPOVEHICULO_LABEL, tipoVehiculo);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<TipoVehiculo> page = null;
        if (q == null || q.isEmpty()) {
            page = tipoVehiculoService.listarActivos(paginable, excel != null);
        } else {
            page = tipoVehiculoService.listarActivos(paginable, q, excel != null);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/tipoVehiculo/listado");
        modelo.addObject(TIPOVEHICULO_LABEL, new TipoVehiculoModel());
        return modelo;
    }

}
