package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ChoferModel;
import com.tcargo.web.servicios.ChoferService;
import com.tcargo.web.servicios.TipoDocumentoService;
import com.tcargo.web.servicios.UsuarioService;
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
@RequestMapping("/api/chofer")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChoferRestController {

    private final ChoferService choferService;
    private final JWTService jwtService;
    private final TipoDocumentoService tipoDocumentoService;
    private final UsuarioService usuarioService;

    @GetMapping("/listado")
    public Map<String, Object> listar(@RequestParam(value = "q", required = false) String query, Pageable pageable, HttpServletRequest request,
                                      HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        response.setStatus(OK.value());
        res.put(CHOFERES_LABEL, choferService.listarActivosPorTransportador(idTransportador, pageable, query));
        res.put(CODIGO, OK);
        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> formulario(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        response.setStatus(OK.value());
        res.put(TIPODOCUMENTOS_LABEL, tipoDocumentoService.listarActivosPorPais(idPais));
        return res;
    }

    @PostMapping("/guardar")
    public Map<String, Object> guardar(@RequestBody ChoferModel model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();

        try {
            model.getUsuario().setId(usuarioService.guardar(model.getUsuario()).getId());
            Chofer chofer = choferService.guardar(model, idTransportador);
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idChofer", chofer.getId());
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

    @DeleteMapping("/eliminar/{id}")
    public Map<String, Object> eliminar(@PathVariable("id") String id, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            choferService.eliminar(id);
            res.put(CODIGO, OK);
            response.setStatus(OK.value());
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

}
