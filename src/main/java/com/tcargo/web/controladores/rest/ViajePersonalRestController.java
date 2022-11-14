package com.tcargo.web.controladores.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.entidades.ViajePersonal;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ViajePersonalModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.servicios.ChoferService;
import com.tcargo.web.servicios.TipoCargaService;
import com.tcargo.web.servicios.TransportadorService;
import com.tcargo.web.servicios.ViajePersonalService;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/viajepersonal")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ViajePersonalRestController {

    private final ChoferService choferService;
    private final JWTService jwtService;
    private final TipoCargaService tipoCargaService;
    private final TransportadorService transportadorService;
    private final TransportadorRepository transportadorRepository;
    private final ViajePersonalService viajePersonalService;
    private static final Log log = LogFactory.getLog(ViajePersonalRestController.class);

    @PostMapping("/guardar")
    public Map<String, Object> guardar(@RequestBody ViajePersonalModel viaje, HttpSession session,
                                       HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(usuario.getId());

        return guardarViaje(viaje, response, res, transportador);
    }


    private Map<String, Object> guardarViaje(ViajePersonalModel model, HttpServletResponse response, Map<String, Object> res, Transportador transportador) {
        try {
            model.setIdTransportador(transportador.getId());
            ViajePersonal v = viajePersonalService.guardar(model);
            choferService.addViajePersonal(v);
            if (model.getId() == null || model.getId().equals("")) {
                transportador.getViajesPersonales().add(v);
                transportadorRepository.save(transportador);
            }
            res.put("id", v.getId());
            response.setStatus(OK.value());
        } catch (WebException e) {
            log.error(e.getMessage());
            res.put("error", e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        }

        return res;
    }

    @PostMapping(value = "/actualizarestado", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> actualizarEstado(@RequestBody ViajePersonalModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        ViajePersonal v = viajePersonalService.porIdEntidad(model.getId());

        if (v == null) {

            res.put(ERROR, "Entidad no encontrada");
            response.setStatus(INTERNAL_SERVER_ERROR.value());

        } else if (v.getEstadoViaje() == model.getEstadoViaje() || v.getEstadoViaje().getNumber() >= model.getEstadoViaje().getNumber()) {

            res.put(ERROR, "Estado no valido");
            response.setStatus(INTERNAL_SERVER_ERROR.value());

        } else {

            try {
                res.put("Viaje", viajePersonalService.actualizarEstado(v.getId(), model.getEstadoViaje()));
            } catch (WebException e) {
                res.put(ERROR, e.getMessage());
                response.setStatus(INTERNAL_SERVER_ERROR.value());
            }
            response.setStatus(OK.value());

        }
        return res;

    }

    @PostMapping("/chofer/listado")
    public Map<String, Object> viajesPersonalesChofer(@RequestBody(required = false) BusquedaViajeModel busqueda, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idChofer = token.getClaim("idChofer").asString();
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();

        if (busqueda == null) {
            busqueda = new BusquedaViajeModel();
        }

        busqueda.setChofer(idChofer);
        res.put(VIAJES_LABEL, viajePersonalService.buscarPorCriteriosPropios(Pageable.unpaged(), busqueda, choferService.buscarPorId(idChofer).getTransportador()).getContent());
        res.put(ESTADOS_LABEL, EstadoViaje.values());
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPais(idPais));
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

}
