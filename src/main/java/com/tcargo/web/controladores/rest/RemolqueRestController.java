package com.tcargo.web.controladores.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tcargo.web.entidades.Remolque;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RemolqueModel;
import com.tcargo.web.servicios.RemolqueService;
import com.tcargo.web.servicios.TipoCargaService;
import com.tcargo.web.servicios.TipoRemolqueService;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/remolque")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemolqueRestController {

    private final JWTService jwtService;
    private final RemolqueService remolqueService;
    private final TipoCargaService tipoCargaService;
    private final TipoRemolqueService tipoRemolqueService;

    @GetMapping("/listado")
    public Map<String, Object> listar(@RequestParam(value = "q", required = false) String query, Pageable pageable, HttpServletRequest request,
                                      HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportadora = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();

        if (query != null && !query.isEmpty()) {
            res.put(REMOLQUES_LABEL, remolqueService.listarActivos(pageable, query));
        } else {
            res.put(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportador(idTransportadora, pageable));
        }

        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> formulario(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();
        String regex = token.getClaim(REGEX_PATENTE).asString();

        res.put(REGEX_PATENTE, regex);
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(TIPOREMOLQUES_LABEL, tipoRemolqueService.activosPorPaisModel(idPais));
        response.setStatus(OK.value());
        return res;
    }

    @PostMapping("/guardar")
    public Map<String, Object> functionName(@RequestBody RemolqueModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            Remolque remolque = remolqueService.guardar(model);
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idRemolque", remolque.getId());
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

    @DeleteMapping("/eliminar/{id}")
    public Map<String, Object> eliminar(@PathVariable String id, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            remolqueService.eliminar(id);
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
        } catch (WebException we) {
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
            response.setStatus(BAD_REQUEST.value());
        }

        return res;
    }

}
