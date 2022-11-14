package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.servicios.DadorDeCargaService;
import com.tcargo.web.servicios.DashboardService;
import com.tcargo.web.servicios.TransportadorService;
import com.tcargo.web.servicios.UsuarioService;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.CODIGO;
import static com.tcargo.web.utiles.Textos.ID_TRANSPORTADOR_LABEL;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DashboardRestController {

    private final DadorDeCargaService dadorDeCargaService;
    private final DashboardService dashboardService;
    private final JWTService jwtService;
    private final TransportadorService transportadorService;
    private final UsuarioService usuarioService;

    @GetMapping("/transportador")
    public Map<String, Object> armarDashboardTransportador(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportadora = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        Transportador transportador = transportadorService.buscarEntidad(idTransportadora);
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put("data", dashboardService.armarReporteTransportador(transportador));
        return res;
    }

    @GetMapping("/chofer")
    public Map<String, Object> armarDashboardChofer(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idUsuario = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put("data", dashboardService.armarReporteChofer(usuarioService.buscar(idUsuario)));
        return res;
    }

    @GetMapping("/dador")
    public Map<String, Object> armarDashboardDador(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idDador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        DadorDeCarga dador = dadorDeCargaService.buscarPorIdUsuario(idDador);
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put("data", dashboardService.armarReporteDador(dador));
        return res;
    }

}
