package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModelPerfil;
import com.tcargo.web.servicios.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.util.Locale;

import static com.tcargo.web.utiles.Roles.getRolActual;
import static com.tcargo.web.utiles.Textos.*;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DashboardController {

    private final TransportadorService transportadorService;
    private final DadorDeCargaService dadorDeCargaService;
    private final UsuarioService usuarioService;
    private final PaisService paisService;
    private final DashboardService dashboardService;
    private final Log log = LogFactory.getLog(DashboardController.class);
    private final MessageSource messages;

    @GetMapping(value = {"", "/"})
    public RedirectView dashboardResolver(HttpSession session) {
        String rolActual;
        try {
            rolActual = getRolActual(session);
        } catch (WebException e) {
            log.error(e.getMessage());
            return new RedirectView("/logout");
        }

        return new RedirectView("/dashboard/" + rolActual);
    }

    @GetMapping("/" + DADORDECARGA_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_DADOR_CARGA','ROLE_AMBAS','ROLE_ADMIN_DADOR','ROLE_ADMIN_AMBAS')")
    public ModelAndView dador(HttpSession session, @RequestParam(value = "change", required = false) boolean change, RedirectAttributes flash, Locale locale) {
        if (checkAndChangeRolActual(session, change, DADORDECARGA_LABEL)) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }

        ModelAndView modelo = new ModelAndView("dashboard-dador");
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        DadorDeCarga d = dadorDeCargaService.buscarPorIdUsuario(usuario.getId());
        
        if(d == null) {
        	UsuarioModelPerfil user = null;
        	try {
                user = usuarioService.armarUsuarioParaEdicionPerfil(usuario.getId());
            } catch (WebException e) {
                e.printStackTrace();
            }
            flash.addFlashAttribute("alertRedirect", messages.getMessage("dash.back.error.completar.datos", null, locale));

            modelo = new ModelAndView("edicion-perfil");
            modelo.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivos());
            modelo.addObject(ROLES_LABEL, Rol.getRolesParaRegistro());
            modelo.addObject(USUARIO_LABEL, user);
            modelo.addObject(PAISES_LABEL, paisService.listarActivos());
            return modelo;
        }

        if (d.getNombre() == null) {
            flash.addFlashAttribute("alertRedirect", messages.getMessage("dash.back.error.completar.datos", null, locale));
            return new ModelAndView("redirect:/usuario/perfil/?id=" + usuario.getId());
        }

        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteDador(d));
        return modelo;
    }

    @GetMapping("/" + TRANSPORTADOR_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_TRANSPORTADOR','ROLE_AMBAS','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS')")
    public ModelAndView transportador(HttpSession session, @RequestParam(value = "change", required = false) boolean change, RedirectAttributes flash, Locale locale) {
        if (checkAndChangeRolActual(session, change, TRANSPORTADOR_LABEL)) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }

        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());

        if (t.getNombre() == null) {
            flash.addFlashAttribute("alertRedirect", messages.getMessage("dash.back.error.completar.datos", null, locale));
            return new ModelAndView("redirect:/usuario/perfil/?id=" + usuario.getId());
        }

        ModelAndView modelo = new ModelAndView("dashboard-transportador");
        modelo.addObject(TRANSPORTADOR_LABEL, t);
        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteTransportador(t));

        return modelo;
    }

    @GetMapping("/" + ADMINISTRADOR_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS')")
    public ModelAndView administrador(HttpSession session, @RequestParam(value = "change", required = false) boolean change) {
        if (checkAndChangeRolActual(session, change, ADMINISTRADOR_LABEL)) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }

        ModelAndView modelo = new ModelAndView("dashboard-admin");
        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteSuperAdmin());
        return modelo;
    }

    @GetMapping("/" + INVITADO_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
    public ModelAndView invitado(HttpSession session, @RequestParam(value = "change", required = false) boolean change) {
        if (checkAndChangeRolActual(session, change, INVITADO_LABEL)) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }
        ModelAndView modelo = new ModelAndView("dashboard-admin");
        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteSuperAdmin());
        return modelo;
    }

    @GetMapping("/" + ADMINISTRADORLOCAL_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR_LOCAL')")
    public ModelAndView administradorlocal(HttpSession session, @RequestParam(value = "change", required = false) boolean change) {
        if (checkAndChangeRolActual(session, change, ADMINISTRADORLOCAL_LABEL)) {
            return new ModelAndView(new RedirectView("/dashboard"));
        }
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        ModelAndView modelo = new ModelAndView("dashboard-admin-local");
        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteAdminLocal(u.getPais()));
        return modelo;
    }

    @GetMapping("/" + SIN_ROL_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_SIN_ROL')")
    public ModelAndView sinRol(HttpSession session, RedirectAttributes flash, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        UsuarioModelPerfil user = null;
        try {
            user = usuarioService.armarUsuarioParaEdicionPerfil(usuario.getId());
        } catch (WebException e) {
            e.printStackTrace();
        }
        flash.addFlashAttribute("alertRedirect", messages.getMessage("dash.back.error.completar.datos", null, locale));

        ModelAndView modelo = new ModelAndView("edicion-perfil");
        modelo.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivos());
        modelo.addObject(ROLES_LABEL, Rol.getRolesParaRegistro());
        modelo.addObject(USUARIO_LABEL, user);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        return modelo;
    }

    @GetMapping("/" + CHOFER_LABEL)
    @PreAuthorize("hasAnyRole('ROLE_CHOFER')")
    public ModelAndView chofer(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        UsuarioModelPerfil user = null;

        try {
            user = usuarioService.armarUsuarioParaEdicionPerfil(usuario.getId());
        } catch (WebException e) {
            e.printStackTrace();
        }

        ModelAndView modelo;
        if (user != null && user.getCuit() != null && !user.getCuit().isEmpty() && user.getIdUbicacion() != null && !user.getIdUbicacion().isEmpty() && user.getNombre() != null && !user.getNombre().isEmpty()) {
            modelo = new ModelAndView("dashboard-chofer");
        } else {

            modelo = new ModelAndView("edicion-perfil");
            modelo.addObject(TRANSPORTADORES_LABEL, transportadorService.listarActivos());

        }

        modelo.addObject(ROLES_LABEL, Rol.getRolesParaRegistro());
        modelo.addObject(USUARIO_LABEL, user);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());
        modelo.addObject(PAGE_LABEL, dashboardService.armarReporteChofer(usuario));
        return modelo;
    }

    private boolean checkAndChangeRolActual(HttpSession session, boolean change, String rolLabel) {
        if (session.getAttribute(ROL_ACTUAL_LABEL) == null ||
                (!session.getAttribute(ROL_ACTUAL_LABEL).equals(rolLabel) && !change)) {
            return true;
        }

        if (change) {
            session.setAttribute(ROL_ACTUAL_LABEL, rolLabel);
        }
        return false;
    }

}
