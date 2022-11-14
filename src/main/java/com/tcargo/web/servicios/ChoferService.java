package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ChoferConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ChoferModel;
import com.tcargo.web.repositorios.ChoferRepository;
import com.tcargo.web.repositorios.TransportadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ChoferService {

	private final ChoferConverter choferConverter;
	private final ChoferRepository choferRepository;
	private final TransportadorRepository transportadorRepository;
	private final EmailService emailService;
	private final MessageSource messages;

	@Autowired
	public ChoferService(ChoferConverter choferConverter, ChoferRepository choferRepository, TransportadorRepository transportadorRepository, /* UsuarioService usuarioService, */ EmailService emailService, MessageSource messages) {
		this.choferConverter = choferConverter;
		this.choferRepository = choferRepository;
		this.transportadorRepository = transportadorRepository;
		this.emailService = emailService;
		this.messages = messages;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { WebException.class, Exception.class })
	public Chofer guardar(ChoferModel model, String idTransportador) throws WebException {
		Transportador transportador = transportadorRepository.getOne(idTransportador);
		model.getUsuario().setRol(Rol.CHOFER);
		model.getUsuario().setIdPais(transportador.getUsuario().getPais().getId());
		Chofer chofer = choferConverter.modeloToEntidad(model);
		validarChofer(chofer);
		chofer.setModificacion(new Date());
		chofer = choferRepository.save(chofer);

		if (!transportador.getChoferes().contains(chofer)) {
			transportador.getChoferes().add(chofer);
		}
		transportadorRepository.save(transportador);

		if (model.getId() == null || model.getId().isEmpty()) {
			emailService.mensaje(chofer.getUsuario().getMail(), chofer.getUsuario().getNombre(), "Bienvenido a T-cargo", "La transportadora " + transportador.getNombre() + " ha dado te ha dado de alta en la plataforma. Ingresa para para utilizarla.");
		}

		return chofer;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { WebException.class, Exception.class })
	public Chofer edit(ChoferModel model) {
		Chofer chofer = choferConverter.modeloToEntidad(model);
		return choferRepository.save(chofer);
	}

	private void validarChofer(Chofer chofer) throws WebException {
		final Locale locale = LocaleContextHolder.getLocale();
		if (chofer.getEliminado() != null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.dado.baja", null, locale));
		}

		if (chofer.getUsuario() == null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.usuario", null, locale));
		}

		if (chofer.getUsuario().getNombre() == null || chofer.getUsuario().getNombre().isEmpty()) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.nombre", null, locale));
		}

		if (chofer.getTipoDocumento() == null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.tipo.doc", null, locale));
		}

		if (chofer.getDocumento() == null || chofer.getDocumento().isEmpty()) {
			throw new WebException(messages.getMessage("pchofer.back.serv.error.documento.empty", null, locale));
		}

		if (chofer.getUsuario().getTelefono() == null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.telefono.empty", null, locale));
		}
		Chofer resultado = choferRepository.buscarPorDocumento(chofer.getDocumento());

		if (resultado != null && !resultado.getId().equals(chofer.getId())) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.registrado", null, locale));
		}

		if (chofer.getTransportador() == null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.transportador", null, locale));
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { WebException.class, Exception.class })
	public Chofer eliminar(String id) throws WebException {
		Chofer chofer = choferRepository.getOne(id);
		final Locale locale = LocaleContextHolder.getLocale();

		if (chofer.getEliminado() != null) {
			throw new WebException(messages.getMessage("chofer.back.serv.error.dado.baja", null, locale));
		}

		chofer.setEliminado(new Date());
		return choferRepository.save(chofer);
	}

	public Page<Chofer> listarActivos(Pageable paginable, String q) {
		return choferRepository.buscarActivos(paginable, "%" + q + "%");
	}

	public Page<Chofer> listarActivos(Pageable paginable) {
		return choferRepository.buscarActivos(paginable);
	}

	public Page<Chofer> listarActivosPorTransportador(String id, Pageable paginable, String q) {
		if (q != null && !q.isEmpty()) {
			q = "%" + q + "%";
			return choferRepository.buscarActivosPorTransportadorConQuery(paginable, id, q);
		}
		return choferRepository.buscarActivosPorTransportador(id, paginable);
	}

	public List<Chofer> listarActivosPorTransportadorList(String id) {
		return choferRepository.buscarActivosPorTransportadorList(id);
	}

	public List<Chofer> listarActivos() {
		return choferRepository.buscarActivos();
	}

	public Chofer buscarPorId(String id) {
		return choferRepository.findById(id).orElse(null);
	}

	public ChoferModel buscar(String id) {
		Chofer chofer = choferRepository.getOne(id);
		return choferConverter.entidadToModelo(chofer);
	}

	public List<ChoferModel> listarActivosPorTransportadorModelList(String id) {
		List<Chofer> choferes = listarActivosPorTransportadorList(id);
		return choferConverter.entidadesToModelos(choferes);
	}

	public Chofer buscarPorUsuario(String idUsuario) {
		return choferRepository.buscarPorUsuarioId(idUsuario);
	}

	public Chofer addViaje(Viaje viaje) {
		Chofer c = choferRepository.buscarPorId(viaje.getChofer().getId());
		if (c.getViajes() == null || c.getViajes().isEmpty()) {
			c.setViajes(new ArrayList<>());
		}
		c.getViajes().add(viaje);
		return choferRepository.save(c);
	}

	public Chofer addViajePersonal(ViajePersonal viaje) {
		Chofer c = choferRepository.buscarPorId(viaje.getChofer().getId());
		if (c.getViajes() == null || c.getViajes().isEmpty()) {
			c.setViajes(new ArrayList<>());
		}
		c.getViajesPersonales().add(viaje);
		return choferRepository.save(c);
	}

	public ChoferModel buscarPorUsuarioModel(String idUsuario) {
		Chofer c = choferRepository.buscarPorUsuarioId(idUsuario);
		if(c != null) {
			return choferConverter.entidadToModelo(c);
		}else {
			return null;
		}
	}

	public Chofer asignarTransportadora(Transportador t, Usuario c) {
		Chofer chofer = choferRepository.buscarPorUsuarioId(c.getId());
		chofer.setTransportador(t);
		return choferRepository.save(chofer);
	}

	public boolean esChoferDeOtroTransportador(String idChofer, String idTransportador) {
		return !buscar(idChofer).getIdTransportador().equals(idTransportador);
	}

}
