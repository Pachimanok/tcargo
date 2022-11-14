package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.repositorios.UsuarioRepository;
import com.tcargo.web.servicios.DadorDeCargaService;
import com.tcargo.web.servicios.TransportadorService;
import com.tcargo.web.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.web.util.UriUtils.decode;

@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LoginController {

	private final UsuarioService usuarioService;
	private final DadorDeCargaService dadorDeCargaService;
	private final TransportadorService transportadorService;
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final OAuth2AuthorizedClientService authorizedClientService;
	private final MessageSource messages;

	private final Log log = LogFactory.getLog(LoginController.class);
	private final Map<String, String> oauth2AuthenticationUrls = new HashMap<>();

	private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorize-client";

	@GetMapping("/login")
	public ModelAndView login(Model model, @RequestParam(required = false) String error,
			@RequestParam(required = false) String logout, @RequestParam(value = "r", required = false) boolean r,
			@RequestParam(value = "ce", required = false) boolean ce, Locale locale, Principal principal) {
		log.info("METODO: login -- PARAMETROS: " + error + " " + logout + " " + r + " " + ce);

		if (principal != null) {
			return new ModelAndView(new RedirectView("/dashboard"));
		}

		if (error != null && error.equals("")) {
			error = messages.getMessage("login.back.error.uno", null, locale);
		}

		if (r) {
			model.addAttribute("success", messages.getMessage("login.back.success", null, locale));
		}
		if (ce) {
			model.addAttribute("success", messages.getMessage("login.back.clave.success", null, locale));
		}
		model.addAttribute(ERROR, error);
		model.addAttribute(LOGOUT_LABEL, logout);

		Iterable<ClientRegistration> clientRegistrations = null;
		ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
		if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
			clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
		}

		clientRegistrations.forEach(registration -> oauth2AuthenticationUrls.put(registration.getClientName(),
				AUTHORIZATION_REQUEST_BASE_URI + "/" + registration.getRegistrationId()));
		model.addAttribute("urls", oauth2AuthenticationUrls);

		return new ModelAndView("login");
	}

	@GetMapping("/loginSuccessOauth2")
	public String getLoginInfo(HttpSession session, Model model, OAuth2AuthenticationToken authentication,
			HttpServletRequest request) throws WebException {
		log.info("METODO: LOGIN INFO ");
		OAuth2AuthorizedClient client = authorizedClientService
				.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
		String userInfoEndpointUri = client.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
		String email = "";
		String nombre = "";

		if (!StringUtils.isEmpty(userInfoEndpointUri)) {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
			HttpEntity<String> entity = new HttpEntity<>("", headers);
			ResponseEntity<Map> response = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity,
					Map.class);
			Map userAttributes = response.getBody();
			nombre = userAttributes.get("name").toString();
			email = userAttributes.get("email").toString();
		}

		Usuario usuario = usuarioService.buscarPorEmail(email);

		if (usuario == null) {
			usuario = usuarioService.registrarConRedes(email, nombre);
		}

		UserDetails ud = usuarioService.loadUserByUsername(usuario.getMail());
		Authentication authentication2 = new UsernamePasswordAuthenticationToken(ud.getUsername(), null,
				ud.getAuthorities());
		request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext());
		SecurityContextHolder.getContext().setAuthentication(authentication2);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return "redirect:/login";
		} else {
			Usuario u = (Usuario) session.getAttribute(USUARIO_LABEL);

			if (u == null || !u.isVerificado()) {
				return "redirect:/login?error";
			} else {
				return "redirect:/dashboard";
			}
		}
	}

	@GetMapping({ "/loginsuccess", "/" })
	public String success(HttpSession session) {
		log.info("METODO: LOGIN SUCCESS ");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return "redirect:/login";
		} else {
			Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
			if (usuario == null) {
				return "redirect:/login";
			} else {
				if (usuario.getRol().equals(Rol.AMBAS)) {
					return "redirect:/pedido/cargas";
				} else if (usuario.getRol().equals(Rol.DADOR_CARGA)) {
					return "redirect:/viaje/listado";
				} else if (usuario.getRol().equals(Rol.TRANSPORTADOR)) {
					return "redirect:/pedido/cargas";
				} else if (usuario.getRol().equals(Rol.CHOFER)) {
					return "redirect:/dashboard/chofer";
				} else {
					return "redirect:/dashboard";
				}
			}
		}
	}

	@GetMapping(value = "/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login?logout";
	}

	@GetMapping("/recuperar")
	public ModelAndView recuperarClave(ModelAndView mav) {
		mav.setViewName("recuperar-clave");
		return mav;
	}

	@PostMapping("/recuperar")
	public String recuperarClavePost(RedirectAttributes attributes, @RequestBody String body) {

		if (body.equals("email=")) {
			attributes.addFlashAttribute("text.recuperarClave.form.span.correoNoValido", "Correo no váildo");
			return "redirect:/recuperar";
		}

		String email = decode(body.split("=")[1], StandardCharsets.UTF_8.name());

		try {
			log.info(usuarioService.recuperarClave(usuarioService.buscarPorEmail(email)));
		} catch (NoSuchMessageException e) {
			final Locale locale = LocaleContextHolder.getLocale();
			attributes.addFlashAttribute(ERROR, messages.getMessage("usuario.back.error.al.guardar", null, locale) + e.getMessage());
			return "redirect:/recuperar";
		} catch (WebException e) {
			final Locale locale = LocaleContextHolder.getLocale();
			attributes.addFlashAttribute(ERROR, messages.getMessage("usuario.back.error.al.guardar", null, locale) + e.getMessage());
			return "redirect:/recuperar";
		}
		return "redirect:/login?r=true";
	}

	@GetMapping("/r/recuperar")
	public ModelAndView cambiarClave(@RequestParam("t") String token) {
		ModelAndView mav = new ModelAndView("recuperar-clave");
		try {
			UsuarioModel model = usuarioService.getFromPassRecoveryToken(token);
			mav.addObject(USUARIO_LABEL, model);
		} catch (NullPointerException npe) {
			mav.addObject(ERROR, "Token inválido o vencido. Solicite uno nuevo aquí abajo.");
		}
		return mav;
	}

	@PostMapping("/cambiar")
	public ModelAndView cambioClave(@Valid @ModelAttribute(USUARIO_LABEL) UsuarioModel model, Locale locale) {
		if (!model.getClave1().equals(model.getClave2())) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("recuperar-clave");
			mav.addObject(USUARIO_LABEL, model);
			mav.addObject(ERROR, messages.getMessage("login.back.claves.diff", null, locale));

			return mav;
		}

		try {
			usuarioService.cambiarClave(model);
		} catch (WebException e) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("recuperar-clave");
			mav.addObject(USUARIO_LABEL, model);
			mav.addObject(ERROR, e.getMessage());

			return mav;
		}

		return new ModelAndView(new RedirectView("/login?ce=true"));
	}

	@GetMapping("/redes-login")
	public String loginConRedes(@RequestParam String email, Model model, HttpSession session) {
		Usuario usuario = usuarioService.buscarPorEmail(email);

		if (usuario != null) {

			return (success(session));

		}
		return null;
	}

	@GetMapping("/basesycondiciones")
	public String bases() {
		return "bases-condiciones";
	}

}
