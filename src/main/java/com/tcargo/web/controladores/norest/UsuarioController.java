package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.modelos.UsuarioModelPerfil;
import com.tcargo.web.modelos.UsuarioModelReporte;
import com.tcargo.web.modelos.busqueda.BusquedaUsuarioModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.TransportadorService;
import com.tcargo.web.servicios.UsuarioService;
import com.tcargo.web.utiles.Textos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/usuario")
public class UsuarioController extends Controlador {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PaisService paisService;

    @Autowired
    private MessageSource messajes;

    @Autowired
    private TransportadorService transportadorService;

    public UsuarioController() {
        super("usuario-list", "usuario-form");
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(USUARIO_LABEL) UsuarioModel usuario, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(user.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol().toString().equals("ADMINISTRADOR_LOCAL")) {
                    usuario.setIdPais(u.getPais().getId());
                }
                usuarioService.guardar(usuario);
                return "redirect:/usuario/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(USUARIO_LABEL, usuario);
            modelo.addAttribute(ERROR, messajes.getMessage("usuario.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(USUARIO_LABEL, usuario);
            modelo.addAttribute(ERROR, messajes.getMessage("usuario.back.error.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        List<Rol> roles = Arrays.asList(Rol.DADOR_CARGA, Rol.AMBAS, Rol.TRANSPORTADOR, Rol.ADMINISTRADOR, Rol.ADMINISTRADOR_LOCAL);
        modelo.addAttribute(ROLES_LABEL, roles);
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(USUARIO_LABEL) UsuarioModel usuario, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        usuario = usuarioService.buscarModel(usuario.getId());
        try {
            usuarioService.eliminar(usuario.getId());
            return "redirect:/usuario/listado";
        } catch (WebException e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute(ERROR, messajes.getMessage("usuario.back.error.al.eliminar", null, locale) + e.getMessage());
            return vistaFormulario;
        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute(ERROR, messajes.getMessage("usuario.back.error.al.eliminar.inespereador", null, locale));
            return vistaFormulario;
        }
    }


    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        UsuarioModel usuario = new UsuarioModel();
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            usuario = usuarioService.buscarModel(id);
        }

        modelo.addObject(USUARIO_LABEL, usuario);
        modelo.addObject(PEDIDO_LABEL, new Pedido());
        List<Rol> roles = Arrays.asList(Rol.DADOR_CARGA, Rol.TRANSPORTADOR, Rol.AMBAS, Rol.ADMINISTRADOR, Rol.ADMINISTRADOR_LOCAL, Rol.INVITADO);
        modelo.addObject(ROLES_LABEL, roles);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @ModelAttribute BusquedaUsuarioModel busqueda, @RequestParam(required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        String idUsuario = ((Usuario) session.getAttribute(USUARIO_LABEL)).getId();
        busqueda.setIdUsuario(idUsuario);
        Page<UsuarioModelReporte> page = usuarioService.buscarPorCriterios(paginable, busqueda, excel != null);
        modelo.addObject(BUSCADOR_LABEL, busqueda);
        modelo.addObject(PAISES_LABEL, paisService.listarActivosModel());
        modelo.addObject(ROLES_LABEL, Arrays.asList(Rol.ADMINISTRADOR, Rol.DADOR_CARGA, Rol.TRANSPORTADOR, Rol.AMBAS, Rol.CHOFER, Rol.INVITADO));
        modelo.addObject(URL_LABEL, "/usuario/listado" + busqueda.toString());
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(USUARIO_LABEL, new UsuarioModel());
        return modelo;
    }

    @GetMapping("/papelera")
    public ModelAndView papelera(Pageable paginable) {
        ModelAndView modelo = new ModelAndView(vistaListado);

        Page<Usuario> page = usuarioService.buscarTodos(paginable, null);

        modelo.addObject(URL_LABEL, "/usuario/papelera");
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(USUARIO_LABEL, new UsuarioModel());
        return modelo;
    }

    @GetMapping("/auditoria")
    public ModelAndView auditoria(Pageable paginable) {
        ModelAndView modelo = new ModelAndView(vistaListado);

        Page<Usuario> page = usuarioService.buscarTodos(paginable, null);

        modelo.addObject(URL_LABEL, "/usuario/auditoria");
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(USUARIO_LABEL, new UsuarioModel());
        return modelo;
    }

    @PostMapping("/blanquear")
    public String cambiarClave(@ModelAttribute(USUARIO_LABEL) UsuarioModel usuario, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "actualizar");
        try {
            usuarioService.cambiarClave(usuario);
            usuario = usuarioService.buscarModel(usuario.getId());
            model.addAttribute(USUARIO_LABEL, usuario);
            model.addAttribute(EXITO_LABEL, messajes.getMessage("usuario.back.success.cambiar.pass", null, locale));
            return vistaFormulario;
        } catch (WebException e) {
            usuario = usuarioService.buscarModel(usuario.getId());
            model.addAttribute(USUARIO_LABEL, usuario);
            model.addAttribute(ERROR, messajes.getMessage("usuario.back.error.cambiar.pass", null, locale) + e.getMessage());
            return vistaFormulario;
        } catch (Exception e) {
            usuario = usuarioService.buscarModel(usuario.getId());
            model.addAttribute(USUARIO_LABEL, usuario);
            model.addAttribute(ERROR, messajes.getMessage("usuario.back.error.cambiar.pass.inesperado", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAnyRole('ROLE_TRANSPORTADOR', 'ROLE_DADOR_CARGA', 'ROLE_AMBAS', 'ROLE_INVITADO', 'ROLE_CHOFER')")
    public ModelAndView formularioperfil(@RequestParam(required = false) String id, @RequestParam(required = false) String accion, Locale locale, HttpSession session) {
        ModelAndView modelo = new ModelAndView("edicion-perfil");
        UsuarioModelPerfil usuario = new UsuarioModelPerfil();

        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }
        try {
            if (id != null) {
                usuario = usuarioService.armarUsuarioParaEdicionPerfil(id);
            }
        } catch (Exception e) {
            modelo.addObject(ERROR, messajes.getMessage("usuario.back.error.actualizar.usuario", null, locale) + e.getMessage());
        }

        List<Rol> roles = Arrays.asList(Rol.DADOR_CARGA, Rol.TRANSPORTADOR, Rol.AMBAS);
        modelo.addObject(ROLES_LABEL, roles);
        modelo.addObject(USUARIO_LABEL, usuario);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivos());
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }


    @PostMapping("/actualizacionperfil")
    @PreAuthorize("hasAnyRole('ROLE_TRANSPORTADOR', 'ROLE_DADOR_CARGA', 'ROLE_AMBAS', 'ROLE_SIN_ROL', 'ROLE_ADMINISTRADOR', 'ROLE_ADMINISTRADOR_LOCAL', 'ROLE_INVITADO', 'ROLE_CHOFER')")
    public ModelAndView actualizar(HttpSession session, @Valid @ModelAttribute(USUARIO_LABEL) UsuarioModelPerfil usuario, @RequestParam("rol") Rol rol, Locale locale, RedirectAttributes redirectAttributes) {
        if (rol == null) {
            ModelAndView mav = new ModelAndView("edicion-perfil");
            List<Rol> roles = Arrays.asList(Rol.DADOR_CARGA, Rol.TRANSPORTADOR, Rol.AMBAS);
            mav.addObject(ROLES_LABEL, roles);
            mav.addObject(ERROR, messajes.getMessage("usuario.back.error.seleccionar.rol", null, locale));
            mav.addObject(USUARIO_LABEL, usuario);
            mav.addObject(PAISES_LABEL, paisService.listarActivos());
            return mav;
        } else {
            usuario.setRol(rol);
        }

        Usuario enSession = (Usuario) session.getAttribute(USUARIO_LABEL);
        Rol rolUsuario = enSession.getRol();

        try {
            if (usuario.getRol() != rolUsuario && rolUsuario != Rol.INVITADO && rolUsuario != Rol.CHOFER) {
                Usuario u = usuarioService.buscar(usuario.getId());
                u.setRol(usuario.getRol());
                u.setNombre(usuario.getNombre());
                usuarioService.cambioDeRol(u, rolUsuario);
                usuarioService.editarPerfil(usuario);
                return new ModelAndView(new RedirectView("/logout"));
            } else {
                if (rolUsuario == Rol.INVITADO) {
                    usuario.setRol(Rol.INVITADO);
                    usuario.setIdPais(usuarioService.buscar(usuario.getId()).getPais().getId());
                }

                if (rolUsuario == Rol.CHOFER) {
                    usuario.setRol(Rol.CHOFER);
                    usuario.setIdPais(usuario.getIdPais() != null ? usuario.getIdPais() : usuarioService.buscar(usuario.getId()).getPais().getId());
                }

                usuarioService.editarPerfil(usuario);
                redirectAttributes.addFlashAttribute("success",messajes.getMessage("text.edicionPerfil.form.success",null,locale));
                return new ModelAndView(new RedirectView("/"));
            }
        } catch (WebException e) {
            ModelAndView mav = new ModelAndView("edicion-perfil");
            List<Rol> roles = Arrays.asList(Rol.DADOR_CARGA, Rol.TRANSPORTADOR, Rol.AMBAS);
            mav.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivos());;
            mav.addObject(ROLES_LABEL, roles);
            mav.addObject(ERROR, messajes.getMessage("usuario.back.error.actualizar.usuario", null, locale) + e.getMessage());
            mav.addObject(USUARIO_LABEL, usuario);
            mav.addObject(PAISES_LABEL, paisService.listarActivos());
            return mav;
        }
    }

    @GetMapping("/blanquearpassword")
    public String blanquear(@RequestParam("id") String id, RedirectAttributes flash, Locale locale) {

        usuarioService.blanquearPass(id);
        flash.addFlashAttribute(Textos.SUCCESS_LABEL, messajes.getMessage("text.for.all.clave.blanqueada.success", null, locale));
        
        return "redirect:/usuario/listado";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cambiarclave/formulario")
    public String cambiarClave(@RequestParam("id") String id, Model model) {

        UsuarioModel usuario = usuarioService.buscarModel(id);
        usuario.setClave("");
        model.addAttribute(USUARIO_LABEL, usuario);

        return "cambiarclave-form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cambiarclave/guardar")
    public String cambiarClave(@Valid @ModelAttribute(USUARIO_LABEL) UsuarioModel usuario, RedirectAttributes flash, Model model) {

        try {
            usuarioService.cambiarClave(usuario);
            flash.addFlashAttribute("success", "Clave cambiada con exito.!");
            return "redirect:/";
        } catch (WebException e) {
            model.addAttribute(ERROR, e.getMessage());
        }

        return "cambiarclave-form";
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/firebase/token")
    @ResponseBody
    public void firebaseToken(HttpSession session, @Valid @RequestBody String token) {
        Usuario usuario = getUsuario(session);
        usuarioService.guardarTokenFirebase(usuario, token);
    }

}
