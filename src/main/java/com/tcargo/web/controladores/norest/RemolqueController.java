package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Remolque;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RemolqueModel;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.servicios.*;
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
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_TRANSPORTADOR', 'ROLE_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/remolque")
public class RemolqueController extends Controlador {

    private final RemolqueService remolqueService;
    private final TipoCargaService tipoCargaService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TransportadorRepository transportadorRepository;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;
    private final MessageSource messajes;

    @Autowired
    public RemolqueController(RemolqueService remolqueService, TipoCargaService tipoCargaService, TipoRemolqueService tipoRemolqueService,
                              TransportadorRepository transportadorRepository, TransportadorService transportadorService, UsuarioService usuarioService, MessageSource mesages) {
        super("remolque-list", "remolque-form");
        this.remolqueService = remolqueService;
        this.tipoCargaService = tipoCargaService;
        this.tipoRemolqueService = tipoRemolqueService;
        this.transportadorRepository = transportadorRepository;
        this.transportadorService = transportadorService;
        this.usuarioService = usuarioService;
        this.messajes = mesages;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(REMOLQUE_LABEL) RemolqueModel remolque, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                Remolque v = remolqueService.guardar(remolque);
                Transportador t = transportadorRepository.getOne(v.getTransportador().getId());
                t.getRemolques().add(v);
                transportadorRepository.save(t);
                return "redirect:/documentacion/formulario/remolque/" + v.getId();
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messajes.getMessage("remolque.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messajes.getMessage("remolque.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        String transportador = transportadorService.buscar(remolque.getId()).getId();
        modelo.addAttribute(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addAttribute(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivos());
        modelo.addAttribute(TRANSPORTADORES_LABEL, transportador);
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(REMOLQUE_LABEL) RemolqueModel remolque, ModelMap model, Locale locale) {
        try {
            remolqueService.eliminar(remolque.getId());
            return "redirect:/remolque/listado?transportadorid=" + remolque.getIdTransportador();
        } catch (Exception e) {
            model.addAttribute(ERROR, messajes.getMessage("remolque.back.error.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(HttpSession session, Pageable paginable,
                                   @RequestParam(required = false, value = "id") String id, @RequestParam(required = false) String accion, @RequestParam(required = false, value = "transportadorid") String transportadorId) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        RemolqueModel remolque = new RemolqueModel();
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            remolque = remolqueService.buscar(id);
        }
        if (transportadorId != null) {
            remolque.setIdTransportador(transportadorId);
        }
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivosPorPais(paginable, u.getPais().getId(), true));
        modelo.addObject(REMOLQUE_LABEL, remolque);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(USUARIO_LABEL, u);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(Pageable paginable, @RequestParam(required = false) String q,
                               @RequestParam(required = false, value = "transportadorid") String transportadorId,
                               @RequestParam(required = false, value = "userid") String userId) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);

        if (userId != null && !userId.equals("")) {
            transportadorId = transportadorService.buscarPorIdUsuario(userId).getId();
        }

        Page<Remolque> page;
        if (q == null || q.isEmpty()) {
            if (transportadorId != null && !transportadorId.equals("")) {
                page = remolqueService.listarActivosPorTransportador(transportadorId, paginable);
            } else {
                page = remolqueService.listarActivos(paginable);
                modelo.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivosModel());
            }
        } else {
            page = remolqueService.listarActivos(paginable, q);
            modelo.addObject(QUERY_LABEL, q);
        }

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject("transportadorid", transportadorId);

        if (transportadorId != null) {
            modelo.addObject(URL_LABEL, "/remolque/listado?transportadorid=" + transportadorId);
        } else {
            modelo.addObject(URL_LABEL, "/remolque/listado");
        }
        modelo.addObject(REMOLQUE_LABEL, new RemolqueModel());
        return modelo;
    }

}
