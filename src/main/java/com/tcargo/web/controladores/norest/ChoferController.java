package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.entidades.TipoDocumento;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ChoferModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.servicios.ChoferService;
import com.tcargo.web.servicios.TipoDocumentoService;
import com.tcargo.web.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_TRANSPORTADOR', 'ROLE_AMBAS','ROLE_INVITADO')")
@RequestMapping("/chofer")
public class ChoferController extends Controlador {

	private static final String DASHBOARD = "/dashboard";
	private static final String CHOFER_LISTADO = "/chofer/listado";
	private static final String CHOFER_FORMULARIO = "/chofer/formulario";
	private final ChoferService choferService;
	private final TipoDocumentoService tipoDocumentosService;
	private final UsuarioService usuarioService;
	private final TransportadorRepository transportadorRepository;

	@Autowired
	public ChoferController(ChoferService choferService, TipoDocumentoService tipoDocumentoService, UsuarioService usuarioService, TransportadorRepository transportadorRepository) {
		super("chofer-list", "chofer-form");
		this.choferService = choferService;
		this.tipoDocumentosService = tipoDocumentoService;
		this.usuarioService = usuarioService;
		this.transportadorRepository = transportadorRepository;
	}

	@PostMapping("/guardar")
	public RedirectView guardarOEditar(@Valid @ModelAttribute ChoferModel model, BindingResult result, RedirectAttributes attributes, HttpSession session) {
		RedirectView redirectView;

		try {
			if (result.hasErrors()) {
				StringBuilder sb = new StringBuilder();
				for (ObjectError error : result.getAllErrors()) {
					sb.append(error.getDefaultMessage()).append(System.getProperty("line.separator"));
				}
				attributes.addFlashAttribute(ERROR, sb.toString());
				redirectView = new RedirectView(CHOFER_FORMULARIO);
			} else {
				UsuarioModel usuarioChofer = armarModel(model, (String) session.getAttribute(ID_TRANSPORTADOR_LABEL));
				model.getUsuario().setId(usuarioService.guardarUsuarioParaCrearTransportador(usuarioChofer).getId());
				Chofer chofer = choferService.guardar(model, (String) session.getAttribute(ID_TRANSPORTADOR_LABEL));
				redirectView = new RedirectView("/documentacion/formulario/chofer/" + chofer.getId());
			}
		} catch (WebException we) {
			log.error(we.getMessage());
			we.printStackTrace();
			attributes.addFlashAttribute(ERROR, we.getMessage());
			attributes.addFlashAttribute(CHOFER_LABEL, model);
			attributes.addFlashAttribute(TIPODOCUMENTOS_LABEL, tipoDocumentosService.listarActivosPorPais((String) session.getAttribute(ID_PAIS_LABEL)));

			redirectView = new RedirectView(CHOFER_FORMULARIO);
		}

		return redirectView;
	}

	private UsuarioModel armarModel(ChoferModel model, String idTransportador) {
		UsuarioModel usuario = model.getUsuario();
		usuario.setRol(Rol.CHOFER);
		Transportador transportador = transportadorRepository.getOne(idTransportador);
		usuario.setIdPais(transportador.getUsuario().getPais().getId());
		return usuario;
	}

	@PostMapping("/eliminar")
	public RedirectView eliminar(@ModelAttribute ChoferModel model, RedirectAttributes attributes) {
		try {
			choferService.eliminar(model.getId());
			attributes.addFlashAttribute(EXITO_LABEL, "Chofer eliminado correctamente.");
			return new RedirectView(CHOFER_LISTADO);
		} catch (WebException we) {
			attributes.addFlashAttribute(ERROR, we.getMessage());
			return new RedirectView(CHOFER_FORMULARIO);
		}
	}

	@GetMapping("/formulario")
	public ModelAndView form(HttpSession session, Model model) {
		String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
		if (!rol.equals(TRANSPORTADOR_LABEL)) {
			return new ModelAndView(new RedirectView(DASHBOARD));
		}

		ChoferModel chofer = new ChoferModel();
		chofer.setIdTransportador((String) session.getAttribute(ID_TRANSPORTADOR_LABEL));

		ModelAndView mav = new ModelAndView(vistaFormulario);
		mav.addObject(TIPODOCUMENTOS_LABEL, tipoDocumentosService.listarActivosPorPais((String) session.getAttribute(ID_PAIS_LABEL)));
		if (model.asMap().get(CHOFER_LABEL) == null) {
			mav.addObject(CHOFER_LABEL, chofer);
		}
		mav.addObject(TIPODOCUMENTOS_LABEL, tipoDocumentosService.listarActivos());
		mav.addObject(ACCION_LABEL, GUARDAR_LABEL);

		return mav;
	}

	@GetMapping("/formulario/{idChofer}")
	public ModelAndView editarOEliminar(@PathVariable("idChofer") String idChofer, @RequestParam(value = ACCION_LABEL, defaultValue = ACTUALIZAR_LABEL) String accion, HttpSession session) {
		String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
		if (!rol.equals(TRANSPORTADOR_LABEL)) {
			return new ModelAndView(new RedirectView(DASHBOARD));
		}

		ModelAndView mav = new ModelAndView(vistaFormulario);
		mav.addObject(TIPODOCUMENTOS_LABEL, tipoDocumentosService.listarActivosPorPais((String) session.getAttribute(ID_PAIS_LABEL)));
		mav.addObject(CHOFER_LABEL, choferService.buscar(idChofer));
		mav.addObject(ACCION_LABEL, accion);

		return mav;
	}


	@GetMapping("/listado")
	public ModelAndView listado(@RequestParam(value = "q", required = false) String q, Pageable pageable, HttpSession session) {
		String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
		if (!rol.equals(TRANSPORTADOR_LABEL)) {
			return new ModelAndView(new RedirectView(DASHBOARD));
		}
		
		ModelAndView mav = new ModelAndView(vistaListado);
		ordenar(pageable, mav);
		
		String idTransportador = (String) session.getAttribute(ID_TRANSPORTADOR_LABEL);
		mav.addObject(PAGE_LABEL, choferService.listarActivosPorTransportador(idTransportador, pageable, q));
		mav.addObject("transportadorId", idTransportador);
		mav.addObject(URL_LABEL, CHOFER_LISTADO);
		mav.addObject(QUERY_LABEL, q);

		return mav;
	}

	@GetMapping("/listado/{idTransportador}")
	public ModelAndView listadoAdmin(@PathVariable("idTransportador") String idTransportador, @RequestParam(value = "q", required = false) String q, Pageable pageable, HttpSession session) {
		String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
		if (!rol.equals(ADMINISTRADOR_LABEL) && !rol.equals(INVITADO_LABEL)) {
			return new ModelAndView(new RedirectView(DASHBOARD));
		}

		ModelAndView mav = new ModelAndView(vistaListado);
		ordenar(pageable, mav);
		mav.addObject(PAGE_LABEL, choferService.listarActivosPorTransportador(idTransportador, pageable, q));
		mav.addObject(URL_LABEL, "/chofer/listado/" + idTransportador);
		mav.addObject(QUERY_LABEL, q);

		return mav;
	}

}
