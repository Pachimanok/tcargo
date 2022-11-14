package com.tcargo.web.controladores.rest;

import com.google.zxing.WriterException;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.servicios.PedidoService;
import com.tcargo.web.servicios.ValoracionService;
import com.tcargo.web.servicios.jwt.JWTService;
import com.tcargo.web.view.pdf.ContratoRelacion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/valoracion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ValoracionRestController {

    private final ContratoRelacion contratoRelacion;
    private final JWTService jwtService;
    private final PedidoService pedidoService;
    private final ValoracionService valoracionService;

    @PostMapping("/guardar")
    public Map<String, Object> guardar(@ModelAttribute ValoracionModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        try {
            valoracionService.guardar(model);
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(ERROR, we.getMessage());
            res.put(CODIGO, BAD_REQUEST);
        }
        return res;
    }

    @GetMapping("/hechas")
    public Map<String, Object> verValoracionesHechas(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String id = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        res.put(VALORACIONES, valoracionService.buscarPorIdCreadorOrdenadoPorModificacion(id));
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

    @GetMapping("/recibidas")
    public Map<String, Object> verValoracionesRecibidas(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String id = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        res.put(VALORACIONES, valoracionService.buscarPorIdReceptorOrdenadoPorModificacion(id));
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

    @GetMapping(value = "/remito", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> reporte(@RequestParam("idpedido") Long idPedido) {
        Pedido pedido = pedidoService.buscarPorId(idPedido);
        ValoracionModel valoracion = valoracionService.buscarPorIdCreadorYpedidoModel(pedido.getDador().getId(), pedido.getId());

        try {
            ByteArrayInputStream bis = contratoRelacion.contrato(pedido, valoracion);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=remito-conformidad.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (IOException | WriterException e) {
            e.printStackTrace();
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InputStreamResource(new ByteArrayInputStream(e.getMessage().getBytes())));
        }
    }

}
