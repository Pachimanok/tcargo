package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.InvitacionModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.ContraOfertaRepository;
import com.tcargo.web.servicios.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/notificacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotificacionRestController {

    private final CoincidenciaRepository coincidenciaRepository;
    private final CoincidenciaService coinsidenciaService;
    private final ContraOfertaRepository contraOfertaRepository;
    private final ContraOfertaService contraOfertaService;
    private final InvitacionService invitacionService;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;

    private static final Log log = LogFactory.getLog(NotificacionRestController.class);

    @PostMapping(value = "/notificacionContraOfertaVista", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> cambiasEstados(@RequestBody UsuarioModel user) {
        Usuario u = usuarioService.buscar(user.getId());
        List<ContraOferta> co;

        if (user.getRol() == Rol.TRANSPORTADOR) {
            Transportador t = transportadorService.buscarPorIdUsuario(user.getId());
            co = contraOfertaService.listarPorTransportadorNotifCreada(t);
            for (ContraOferta contraOferta : co) {
                contraOferta.setNotificacion(EstadoNotificacion.VISTO);
                contraOfertaRepository.save(contraOferta);
            }
        } else if (user.getRol() == Rol.DADOR_CARGA) {
            co = contraOfertaService.listarPorDadorNotifCreada(u);
            for (ContraOferta contraOferta : co) {
                contraOferta.setNotificacion(EstadoNotificacion.VISTO);
                contraOfertaRepository.save(contraOferta);
            }
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/notificacionvista", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> cambiarUnEstado(@RequestBody ContraOfertaModel contraOferta) {
        ContraOferta co = contraOfertaRepository.getOne(contraOferta.getId());
        co.setNotificacion(EstadoNotificacion.VISTO);
        contraOfertaRepository.save(co);

        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/match/notificacionvista", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> notificacionMatchVisto(@RequestBody CoincidenciaModel match) {
        Coincidencia co = coinsidenciaService.buscarPorId(match.getId());
        co.setNotificacion(EstadoNotificacion.VISTO);
        coincidenciaRepository.save(co);

        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/conformidad/notificacionvista", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> notificacionConformidadVisto(@RequestBody CoincidenciaModel match) {
        Coincidencia co = coinsidenciaService.buscarPorId(match.getId());
        co.setNotificacionConformidad(EstadoNotificacion.VISTO);
        coincidenciaRepository.save(co);

        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/invitacion/notificacionvista", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> notificacionInvitacionVista(@RequestBody InvitacionModel invitacion) {
        InvitacionModel invitacionModel = invitacionService.buscarModel(invitacion.getId());
        invitacionModel.setEstadoNotificacion(EstadoNotificacion.VISTO);
        Map<String, Boolean> response = new HashMap<>();

        try {
            invitacionService.guardar(invitacionModel);
            response.put("ok", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (WebException e) {
            log.error(e.getMessage());
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

}
