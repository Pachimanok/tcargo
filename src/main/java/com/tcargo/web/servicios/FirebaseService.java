package com.tcargo.web.servicios;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.Token;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.repositorios.TokenRepository;

@Service
public class FirebaseService {

	@Autowired
	private TokenRepository tokenRepository;

	private Log log;

	public FirebaseService() {
		this.log = LogFactory.getLog(getClass());
		try {
			inicializar();
		} catch (IOException e) {
			log.error("Error al inicializar firebase: ", e);
		}
	}

	public void inicializar() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {

			File configuracion = ResourceUtils.getFile("classpath:dynamic-digit-206320-firebase-adminsdk-l54gr-466d9a4a7a.json");
			FileInputStream serviceAccount = new FileInputStream(configuracion);

			FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://dynamic-digit-206320.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		}
	}

	@Async
	public void enviar(String token, String descripcion, Map<String, String> datos) {
		try {
			inicializar();
			Message mensaje = Message.builder().setNotification(Notification.builder().setTitle("T-Cargo").setBody(descripcion).build()).putAllData(datos).setToken(token).build();
			String respuesta = FirebaseMessaging.getInstance().send(mensaje);
		} catch (Exception e) {
			log.error("Error al enviar mensaje.", e);
		}
	}
	
	@Async
	public void notificar(String idUsuario, String notificacion, Map<String, String> datos) {
		List<Token> tokens = tokenRepository.buscarPorUsuarioId(idUsuario);
		for (Token token : tokens) {
			enviar(token.getTexto(), notificacion, datos);
		}
	}

	public void match(Coincidencia match) {
		List<Token> tokens = tokenRepository.buscarPorUsuarioId(match.getPedido().getDador().getId());

		HashMap<String, String> datos = new HashMap<>();

		datos.put("tipo", "listo-para-cargar");
		datos.put("origen", match.getPedido().getUbicacionDesde().getDireccion());
		datos.put("destino", match.getPedido().getUbicacionHasta().getDireccion());

		for (Token token : tokens) {
			enviar(token.getTexto(), "Han realizado una oferta en su pedido de carga.", datos);
		}

	}

	public void matchContraOferta(Coincidencia match) {
		HashMap<String, String> datos = new HashMap<>();

		datos.put("tipo", "listo-para-cargar");
		datos.put("origen", match.getPedido().getUbicacionDesde().getDireccion());
		datos.put("destino", match.getPedido().getUbicacionHasta().getDireccion());

		//FIXME Ver el titulo de las notificaciones y ver si esta bien el titulo del metodo.
		notificar(match.getPedido().getDador().getId(), "", datos);
		notificar(match.getTransportador().getUsuario().getId(), "", datos);

	}

	public void contraofertaNueva(String usuarioId, ContraOfertaModel model) {
		HashMap<String, String> datos = new HashMap<>();

		datos.put("tipo", "nueva-contra-oferta");
		datos.put("nuevaOferta", "Tienes una nueva oferta sobre la carga: " + model.getIdPedido());
		
		notificar(usuarioId, "Han realizado una oferta en su pedido de carga.", datos);
		
	}

	public void cambioDeEstadoDeViaje(String idUsuario, Long idPedido, String estadoViaje) {

		HashMap<String, String> datos = new HashMap<>();

		datos.put("tipo", "nuevo-estado-viaje");
		datos.put("nuevoEstadoViaje", "Há cambiabo el estado de viaje del pedido: " + idPedido.toString() + " a " + estadoViaje);

		
		notificar(idUsuario, "Ha cambiado el estado del viaje.", datos);
	
	}

	public void pedidoEditado(String idUsuario, Long idPedido) {
		HashMap<String, String> datos = new HashMap<>();

		datos.put("tipo", "pedido-editado");
		datos.put("pedidoEditado", "Ha cambiado el pedido " + idPedido.toString() + " al que has ofertado.");

		notificar(idUsuario, "Ha cambiado el pedido " + idPedido.toString() + " al que has ofertado.", datos);
	}

}
