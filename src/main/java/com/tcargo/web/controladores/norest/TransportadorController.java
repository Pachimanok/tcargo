package com.tcargo.web.controladores.norest;

import com.tcargo.web.convertidores.UsuarioConverter;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TransportadorModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.modelos.busqueda.BusquedaTransportadorModel;
import com.tcargo.web.servicios.TransportadorService;
import com.tcargo.web.servicios.UbicacionService;
import com.tcargo.web.servicios.UsuarioService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_ADMINISTRADOR_LOCAL', 'ROLE_INVITADO')")
@RequestMapping("/transportador")
public class TransportadorController extends Controlador {

    private final MessageSource messages;
    private final TransportadorService transportadorService;
    private final UbicacionService ubicacionService;
    private final UsuarioConverter usuarioConverter;
    private final UsuarioService usuarioService;

    @Autowired
    public TransportadorController(MessageSource messages, TransportadorService transportadorService, UbicacionService ubicacionService, UsuarioConverter usuarioConverter, UsuarioService usuarioService) {
        super("transportador-list", "transportador-form");
        this.messages = messages;
        this.transportadorService = transportadorService;
        this.ubicacionService = ubicacionService;
        this.usuarioConverter = usuarioConverter;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS','ROLE_TRANSPORTADOR','ROLE_AMBAS')")
    public String guardar(HttpSession session, @Valid @ModelAttribute(TRANSPORTADOR_LABEL) TransportadorModel transportador, @Valid @ModelAttribute(UBICACION_LABEL) UbicacionModel ubicacion, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                UsuarioModel model = usuarioConverter.entidadToModelo(usuario);
                if (ubicacion != null) {
                    ubicacion.setId(ubicacion.getIdProvisiorio());
                    ubicacionService.guardar(ubicacion);
                    model.setIdUbicacion(ubicacion.getIdProvisiorio());
                }

                usuarioService.editar(model);
                transportadorService.guardar(transportador);
                return "redirect:/";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("transportador.back.error.al.guardar", null, locale) + e.getMessage() + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("transportador.back.error.inesperado", null, locale) + e.getMessage());
            log.error(ERROR_INESPERADO, e);
        }

        modelo.addAttribute(UBICACION_LABEL, ubicacion);
        List<Usuario> usuarios = usuarioService.buscarPorRol(Rol.TRANSPORTADOR);
        modelo.addAttribute(USUARIOS_LABEL, usuarios);
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(TRANSPORTADOR_LABEL) TransportadorModel transportador, @Valid @ModelAttribute(UBICACION_LABEL) UbicacionModel ubicacion, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            transportadorService.eliminar(transportador.getId());
            return "redirect:/transportador/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("transportador.back.error.al.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        TransportadorModel transportador = new TransportadorModel();
        UbicacionModel ubicacionModel = new UbicacionModel();

        if (id != null) {
            transportador = transportadorService.buscar(id);
        }

        List<Usuario> usuarios = usuarioService.buscarPorRoles(Arrays.asList(Rol.TRANSPORTADOR, Rol.AMBAS, Rol.ADMIN_AMBAS, Rol.ADMIN_TRANSPORTADOR));
        modelo.addObject(USUARIOS_LABEL, usuarios);
        modelo.addObject(UBICACION_LABEL, ubicacionModel);
        modelo.addObject(TRANSPORTADOR_LABEL, transportador);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, @PageableDefault(size = 20, sort = "t.nombre") Pageable paginable, @ModelAttribute BusquedaTransportadorModel busqueda, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        ordenar(paginable, modelo);

        if (busqueda == null) {
            busqueda = new BusquedaTransportadorModel();
        }

        if (rolActual.equals(ADMINISTRADORLOCAL_LABEL)) {
            busqueda.setIdPais(idPais);
        }

        Page<Transportador> page = transportadorService.buscarFiltrados(paginable, busqueda, excel != null);

        modelo.addObject(BUSCADOR_LABEL, busqueda);
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/transportador/listado" + busqueda.toString());
        return modelo;
    }

}
