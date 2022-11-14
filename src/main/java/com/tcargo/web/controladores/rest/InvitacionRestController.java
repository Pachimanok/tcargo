package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Invitacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.InvitacionModel;
import com.tcargo.web.servicios.EmailService;
import com.tcargo.web.servicios.InvitacionService;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.ERROR;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/invitacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InvitacionRestController {

    private final EmailService emailService;
    private final InvitacionService invitacionService;
    private final Log log = LogFactory.getLog(InvitacionRestController.class);

    @PostMapping(value = "/enviarinvitacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> enviarInvitacion(@RequestBody InvitacionModel model) {
        Map<String, String> response = new HashMap<>();

        try {
            Invitacion i = invitacionService.guardar(model);
            emailService.templateConLinkAltaChofer(new Date(), i.getTransportador().getUsuario().getMail(), "Tenes una solicitud para dar de alta un chofer.", i.getId());
        } catch (WebException e) {
            log.error(e.getMessage());
            response.put(ERROR, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, OK);
    }

}
