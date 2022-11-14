package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static com.tcargo.web.utiles.Textos.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final PaisService paisService;
    private final UsuarioService usuarioService;
    private final AuthenticationManager auth;

    @Autowired
    public RegistroController(PaisService paisService, UsuarioService usuarioService, AuthenticationManager auth) {
        this.paisService = paisService;
        this.usuarioService = usuarioService;
        this.auth = auth;
    }

    @GetMapping("/formulario")
    public String registro(Model modelo, @RequestParam(required = false) String email) {
        UsuarioModel usuario = new UsuarioModel();
        if (email != null && !email.isEmpty()) {
            usuario.setMail(email);
        }
        modelo.addAttribute(USUARIO_LABEL, usuario);
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
        modelo.addAttribute(ROLES_LABEL, Rol.getRolesParaRegistro());
        return REGISTRO_LABEL;
    }
    /* @RequestParam("rol") Rol rol*/
    @PostMapping("/registrar")
    public String registrar
            (@ModelAttribute(USUARIO_LABEL) UsuarioModel usuario,
             Model modelo, RedirectAttributes flash, HttpServletRequest request,@RequestParam("rol") Rol rol) {
        try {
            usuario.setRol(rol != null ? rol : null);
            usuarioService.registroDeUsuario(usuario);
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario.getMail(), usuario.getClave(), usuarioService.loadUserByUsername(usuario.getMail()).getAuthorities());
// 		   	authToken.setDetails(new WebAuthenticationDetails(request));
// 		    
// 		   	Authentication authentication = auth.authenticate(authToken);
// 		    
//
// 	       request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
// 	       SecurityContextHolder.getContext().setAuthentication(authentication);
//           flash.addFlashAttribute("success", "Registro generado con éxito.");
//           return "redirect:/dashboard";
            String mensaje = String.format("¡Bienvenido/a %s a T-Cargo! Se registró con éxito. \n" +
                    "Revise su mail para activar su cuenta.", usuario.getMail());
            flash.addFlashAttribute("success", mensaje);
            return "redirect:/login";
        } catch (WebException e) {
        	modelo.addAttribute(ROLES_LABEL, Rol.getRolesParaRegistro());
            modelo.addAttribute(ERROR, e.getMessage());
            modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());
            return REGISTRO_LABEL;
        }
    }

    @GetMapping("/verificacion")
    public ModelAndView verificarCuenta(@RequestParam String t, RedirectAttributes attributes) {
        try {
            Usuario usuario = usuarioService.verificar(t);
            String mensaje = String.format("¡Bienvenido/a %s a T-Cargo! Tu cuenta ha sido verificada. " +
                    "Ya puedes empezar a usar la plataforma iniciando sesión aquí abajo.", usuario.getNombre());
            attributes.addFlashAttribute("success", mensaje);
            return new ModelAndView(new RedirectView("/login"));
        } catch (NullPointerException npe) {
            ModelAndView mav = new ModelAndView("recuperar-clave");
            mav.addObject("errorVerificacion", "Error al verificar la cuenta. Ingresa tu correo" +
                    " aquí abajo y le reenviaremos el link de verificación.");
            return mav;
        }
    }

    @PostMapping("/verificacion/reenviar")
    public RedirectView reenviar(@RequestParam String email, RedirectAttributes attributes) {
        usuarioService.reenviarCorreoVerificacion(email);
        attributes.addFlashAttribute("success", "Correo de verificación reenviado.");
        return new RedirectView("/login");
    }

}
