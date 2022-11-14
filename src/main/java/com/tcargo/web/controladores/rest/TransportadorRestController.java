package com.tcargo.web.controladores.rest;

import com.tcargo.web.convertidores.TransportadorConverter;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.modelos.TransportadorModel;
import com.tcargo.web.servicios.TransportadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transportadorRest")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TransportadorRestController {

    private final TransportadorService transportadorService;
    private final TransportadorConverter transportadorConverter;

    @GetMapping(value = "/buscarPorUsuario", produces = MediaType.APPLICATION_JSON_VALUE)
    public TransportadorModel buscarPorIdUsuario(@RequestParam("userId") String id) {
        Transportador t = transportadorService.buscarPorIdUsuario(id);
        return transportadorConverter.entidadToModelo(t);
    }


}
