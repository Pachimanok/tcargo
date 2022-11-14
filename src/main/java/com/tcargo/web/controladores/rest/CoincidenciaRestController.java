package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.*;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.PedidoModel;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CoincidenciaRestController {

    private final FirebaseService firebaseService;
    private final ChoferService choferService;
    private final CoincidenciaService coincidenciaService;
    private final ContraOfertaService contraOfertaService;
    private final JWTService jwtService;
    private final PaisService paisService;
    private final PedidoService pedidoService;
    private final RemolqueService remolqueService;
    private final VehiculoService vehiculoService;
    private final EmailService emailService;
    private final ViajeService viajeService;
    private final Log log = LogFactory.getLog(CoincidenciaRestController.class);

    @PostMapping(value = "/guardarDesdeContraOferta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> guardarBasicoDesdeContraOferta(HttpSession session, @RequestBody ContraOfertaModel contraOferta) {
        Map<String, String> response = new HashMap<>();
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        Pais pais = paisService.buscarParaMatch(user.getId());
        CoincidenciaModel match = new CoincidenciaModel();

        try {
            Pedido p = pedidoService.buscarPorId(Long.valueOf(contraOferta.getIdPedido()));
            ContraOfertaModel co = contraOfertaService.buscar(contraOferta.getId());
            match.setCosto(p.getValor());
            if (p.isRecibirOfertas()) match.setCosto(co.getValor());
            match.setIdPedido(Long.valueOf(contraOferta.getIdPedido()));
            match.setDetalle(p.getDescripcion());
            match.setIdTransportador(co.getIdTransportador());
            match.setIdViaje(contraOferta.getComentarios());
            Coincidencia matchPersistido = coincidenciaService.guardar(match, pais);
            response.put("id", matchPersistido.getId());
            emailService.match(matchPersistido.getTransportador().getUsuario().getMail(), matchPersistido.getTransportador().getUsuario().getNombre(), matchPersistido.getPedido().getId());
            emailService.match(matchPersistido.getPedido().getDador().getMail(), matchPersistido.getPedido().getDador().getNombre(), matchPersistido.getPedido().getId());
            //envio de notificacion al dador del viaje y transportador, aviso que se generó match.
            new Thread(() -> firebaseService.matchContraOferta(matchPersistido)).start();

            return new ResponseEntity<>(response, OK);
        } catch (WebException e) {
            log.error(e.getMessage());
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/form")
    public Map<String, Object> formulario(@RequestParam("idPedido") Long idPedido, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        PedidoModel pedido = pedidoService.pedidoModelParaMatchForm(idPedido);
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(PEDIDO_LABEL, pedido);
        res.put(CHOFERES_LABEL, choferService.listarActivosPorTransportadorList(idTransportador));
        res.put(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(idTransportador));
        res.put(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorList(idTransportador));
        return res;
    }

    @PostMapping("/guardar/{idPedido}")
    public Map<String, Object> guardar(@PathVariable Long idPedido, @RequestBody ViajeModel model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        Pais pais = paisService.buscarEntidad(idPais);
        Pedido pedido = pedidoService.buscarPorId(idPedido);
        model.setKms(pedido.getKmTotales());
        model.setIdUbicacionInicial(pedido.getUbicacionDesde().getId());
        model.setIdUbicacionFinal(pedido.getUbicacionHasta().getId());
        model.setIdCarga(pedido.getCarga().getId());

        try {
            CoincidenciaModel coincidenciaModel = coincidenciaService.crearModelo(idPedido);
            Viaje viaje = viajeService.guardar(model);
            coincidenciaModel.setIdViaje(viaje.getId());
            coincidenciaModel.setIdTransportador(idTransportador);
            Coincidencia match = coincidenciaService.guardar(coincidenciaModel, pais);
            pedidoService.asignarTransportadorTrue(idPedido);
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idMatch", match.getId());
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

}
