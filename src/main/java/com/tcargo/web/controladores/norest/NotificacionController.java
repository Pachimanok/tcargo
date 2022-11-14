package com.tcargo.web.controladores.norest;

import com.tcargo.web.servicios.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping("/notificacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotificacionController {

    private final FirebaseService firebaseService;

    @GetMapping("/prueba")
    @ResponseBody
    public String enviar(@RequestParam("id") String idUsuario) {
        firebaseService.notificar(idUsuario, "Notificacion de prueba", new HashMap<>());
        return "Enviado";
    }

}
