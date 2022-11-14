package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.TipoDocumento;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoDocumentoModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TipoDocumentoService;
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
@RequestMapping("/tipoDocumento")
public class TipoDocumentoController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;
    private final TipoDocumentoService tipoDocumentoService;

    @Autowired
    public TipoDocumentoController(MessageSource messages, PaisService paisService, TipoDocumentoService tipoDocumentoService) {
        super("tipoDocumento-list", "tipoDocumento-form");
        this.messages = messages;
        this.paisService = paisService;
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute(TIPODOCUMENTO_LABEL) TipoDocumentoModel tipoDocumento, BindingResult resultado, ModelMap modelo, Locale locale) {
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                tipoDocumentoService.guardar(tipoDocumento);
                return "redirect:/tipoDocumento/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.documento.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("tipo.documento.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TIPODOCUMENTO_LABEL) TipoDocumentoModel tipoDocumento, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            tipoDocumentoService.eliminar(tipoDocumento.getId());
            return "redirect:/tipoDocumento/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("ttipo.documento.back.error.al.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TipoDocumentoModel tipoDocumento = new TipoDocumentoModel();

        if (id != null) {
            tipoDocumento = tipoDocumentoService.buscar(id);
        }

        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        modelo.addObject(TIPODOCUMENTO_LABEL, tipoDocumento);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        Page<TipoDocumento> page;
        if (q == null || q.isEmpty()) {
            page = tipoDocumentoService.listarActivos(paginable, excel != null);
        } else {
            page = tipoDocumentoService.listarActivos(paginable, q, excel != null);
            modelo.addObject(QUERY_LABEL, q);
        }
        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/tipoDocumento/listado");
        modelo.addObject(TIPODOCUMENTOS_LABEL, new TipoDocumentoModel());
        return modelo;
    }

}
