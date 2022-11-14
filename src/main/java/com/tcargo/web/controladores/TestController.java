package com.tcargo.web.controladores;

import com.tcargo.web.servicios.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enviar-correos")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestController {

    private final EmailService emailService;

    private static final String EMAIL = "facrodsteam@gmail.com";

    @GetMapping
    public String enviar() {
        emailService.registroBasico(EMAIL);
        emailService.registroCompleto(EMAIL, "Fernando Cocco");
        emailService.mensaje(EMAIL, "Fernando", "Asunto", "Mensaje");
        return "Mensajes enviados";
    }

}
