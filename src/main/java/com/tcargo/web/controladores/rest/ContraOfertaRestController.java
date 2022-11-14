package com.tcargo.web.controladores.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.busqueda.BusquedaHistorialModel;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/contraoferta")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ContraOfertaRestController {

    private final ContraOfertaService contraOfertaService;
    private final JWTService jwtService;
    private final MonedaService monedaService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;
    private final RemolqueService remolqueService;

    @PostMapping(value = "/aceptarcontraoferta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> aceptarContraoferta(@RequestBody ContraOfertaModel co) {
        Map<String, Boolean> response = new HashMap<>();

        try {
            ContraOferta contraOferta = contraOfertaService.aceptarContraoferta(co.getId());
            contraOfertaService.guardarFinal(contraOferta, co.getIdCreador());

            if (co.getIdPedidoLong() != null) {
                Pedido p = pedidoService.buscarPorId(co.getIdPedidoLong());
                p.setValor(co.getValor());
                pedidoService.asignarTransportadorTrue(p.getId());
                List<ContraOfertaModel> x = contraOfertaService.listarPorPedidoList(p);
                for (ContraOfertaModel c : x) {
                    if (!c.getId().equals(co.getId())) {
                        contraOfertaService.desestimarContraoferta(c.getId());
                    }
                }
            }

            response.put("OK", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("ERROR", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/desestimarcontraoferta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> desestimarContraoferta(@RequestBody ContraOfertaModel co) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            contraOfertaService.desestimarContraoferta(co.getId());
            response.put("OK", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("ERROR", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping("/listado/{idPedido}")
    public Map<String, Object> listar(@PathVariable("idPedido") Long idPedido, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put("contraofertas", contraOfertaService.buscarPorIdPedidoYIdTransportador(idPedido, idTransportador));
        return res;
    }

    @PostMapping("/aceptar")
    public Map<String, Object> aceptar(@RequestParam("contraofertaid") String contraOfertaid, @RequestParam("pedidoid") Long pedidoId,
                                       HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idUsuario = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();

        try {
            ContraOferta contraOferta = contraOfertaService.aceptarContraoferta(contraOfertaid);
            contraOfertaService.guardarFinal(contraOferta, idUsuario);
            pedidoService.asignarTransportadorTrue(pedidoId);
            List<ContraOfertaModel> contraOfertaModels = contraOfertaService.listarPorPedidoList(pedidoService.buscarPorId(pedidoId));
            for (ContraOfertaModel c : contraOfertaModels) {
                if (!c.getId().equals(contraOfertaid)) {
                    contraOfertaService.desestimarContraoferta(c.getId());
                }
            }
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
        } catch (Exception e) {
            response.setStatus(NOT_FOUND.value());
            res.put(CODIGO, NOT_FOUND);
            res.put(ERROR, e.getMessage());
        }

        return res;
    }

    // TODO (COMPLETAR CUANDO ESTEN TODOS LOS ROLES)
    @PostMapping("/guardar")
    public Map<String, Object> guardar(@RequestBody ContraOfertaModel contraOferta, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idUsuario = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        Usuario u = usuarioService.buscar(idUsuario);

        try {
            switch (u.getRol()) {
                case TRANSPORTADOR:
                    ContraOferta co = contraOfertaService.guardar(contraOferta);
                    response.setStatus(OK.value());
                    res.put("idContraOferta", co.getId());
                    res.put(CODIGO, OK);
                    break;
            }
        } catch (WebException e) {
            response.setStatus(NOT_FOUND.value());
            res.put(CODIGO, NOT_FOUND);
            res.put(ERROR, e.getMessage());
        }

        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> formulario(HttpServletRequest request, HttpServletResponse response, @RequestParam("idPedido") Long idPedido) {
        Map<String, Object> res = new HashMap<>();
        response.setStatus(OK.value());
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();
        String idUsuario = token.getClaim("id").asString();
        Rol rol = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ROL_LABEL).as(Rol.class);
        ContraOfertaModel contraOferta = new ContraOfertaModel();
        Pedido pedido = pedidoService.buscarPorId(idPedido);

        if (pedido.isAsignadoATransportador()) {
            response.setStatus(NOT_FOUND.value());
            res.put(CODIGO, NOT_FOUND);
            res.put(ERROR, "Pedido no valido para ofertar");
        } else {
            switch (rol) {
                case TRANSPORTADOR:
                    String idTransportadora = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();

                    res.put(MONEDAS_LABEL, monedaService.listarActivosPorPais(idPais));
                    res.put(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorModel(idTransportadora));
                    res.put(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorModel(idTransportadora));
                    res.put(REQUISITOS_LABEL, pedido.getTipoRequisitos());
                    contraOferta.setIdPedidoLong(pedido.getId());
                    contraOferta.setIdCreador(idUsuario);
                    contraOferta.setIdTransportador(idTransportadora);
                    contraOferta.setIdDador(pedido.getDador().getId());
                    res.put(CONTRAOFERTA_LABEL, contraOferta);
                    break;
            }
        }

        return res;
    }

    @PostMapping("/historial")
    public Map<String, Object> verHistorial(@PageableDefault(size = 20, sort = {"modificacion"}) Pageable pageable,
                                            @RequestBody(required = false) BusquedaHistorialModel busqueda,
                                            HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idTransportador = token.getClaim(ID_TRANSPORTADOR_LABEL).asString();

        if (busqueda == null) {
            busqueda = new BusquedaHistorialModel();
        }

        Page<ContraOfertaModel> page;
        if (idTransportador != null) {
            page = contraOfertaService.buscarPorTransportador(pageable, idTransportador, busqueda);
        } else {
            page = contraOfertaService.buscarPorDador(pageable, token.getClaim("id").asString(), busqueda);
        }
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(CONTRAOFERTAS_LABEL, page);
        res.put(ESTADOS_LABEL, EstadoContraOferta.values());
        return res;
    }

}
