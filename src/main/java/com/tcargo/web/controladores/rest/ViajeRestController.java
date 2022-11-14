package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.EstadoValoracion;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CargaNegativaModel;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.repositorios.ValoracionRepository;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/viaje")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ViajeRestController {

    private final ChoferService choferService;
    private final CoincidenciaRepository matchRepository;
    private final ContraOfertaService contraOfertaService;
    private final EmailService emailService;
    private final FirebaseService firebaseService;
    private final JWTService jwtService;
    private final ModeloService modeloService;
    private final PedidoService pedidoService;
    private final RemolqueService remolqueService;
    private final TipoCargaService tipoCargaService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TipoVehiculoService tipoVehiculoService;
    private final TransportadorRepository transportadorRepository;
    private final TransportadorService transportadorService;
    private final ValoracionRepository valoracionRepository;
    private final VehiculoService vehiculoService;
    private final ViajeService viajeService;
    private static final Log LOG = LogFactory.getLog(ViajeRestController.class);

    @PostMapping("/guardar")
    public Map<String, Object> guardarBasico(@RequestBody ViajeModel viaje, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            Viaje viajePersistido = viajeService.guardar(viaje);
            choferService.addViaje(viajePersistido);
            res.put("id", viajePersistido.getId());
            response.setStatus(OK.value());
        } catch (WebException e) {
            LOG.error(e.getMessage());
            res.put(ERROR, e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        }

        return res;
    }

    @PostMapping("/guardarcarganegativa")
    public Map<String, Object> guardarCargaNegativa(@RequestBody CargaNegativaModel viaje, HttpSession session, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Transportador transportador = transportadorService.buscarPorIdUsuario(usuario.getId());
        return guardarCargaNegativa(viaje, response, res, transportador);
    }

    @PostMapping("/guardarDesdeContraOferta")
    public Map<String, Object> guardarBasicoDesdeContraOferta(@RequestBody ContraOfertaModel contraOferta, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            ContraOfertaModel contraOfertaModel = contraOfertaService.buscar(contraOferta.getId());
            Pedido pedido = pedidoService.buscarPorId(Long.valueOf(contraOferta.getIdPedido()));
            ViajeModel viaje = new ViajeModel();

            if (contraOfertaModel.getIdRemolque() != null && contraOfertaModel.getIdRemolque().isEmpty()) {
                viaje.setIdRemolque(contraOfertaModel.getIdRemolque());
            }
            if (contraOfertaModel.getIdVehiculo() != null && !contraOfertaModel.getIdVehiculo().isEmpty()) {
                viaje.setIdVehiculo(contraOfertaModel.getIdVehiculo());
            }
            if (contraOfertaModel.getIdChofer() != null && !contraOfertaModel.getIdChofer().isEmpty()) {
                viaje.setIdChofer(contraOfertaModel.getIdChofer());
            }


            viaje.setIdCarga(pedido.getCarga().getId());
            viaje.setKms(pedido.getKmTotales());
            viaje.setIdUbicacionInicial(pedido.getUbicacionDesde().getId());
            viaje.setIdUbicacionFinal(pedido.getUbicacionHasta().getId());

            Viaje viajePersistido = viajeService.guardar(viaje);

            if (viajePersistido.getChofer() != null) {
                choferService.addViaje(viajePersistido);
            }

            res.put("id", viajePersistido.getId());

            response.setStatus(OK.value());
        } catch (WebException e) {
            LOG.error(e.getMessage());
            res.put(ERROR, e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        }

        return res;
    }

    @GetMapping("/ver/{id}")
    public Map<String, Object> viaje(@PathVariable("id") String id, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        ViajeModel viaje = viajeService.porIdModel(id);

        if (viaje == null) {
            res.put(ERROR, "Viaje no encontrado");
            res.put(CODIGO, NOT_FOUND);
            response.setStatus(NOT_FOUND.value());
            return res;
        }

        res.put(VIAJE_LABEL, viaje);
        response.setStatus(OK.value());
        return res;
    }

    @PostMapping("/dador")
    public Map<String, Object> mostrarParaDador(@RequestBody(required = false) BusquedaViajeModel busqueda, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();

        if (busqueda == null) {
            busqueda = new BusquedaViajeModel();
        }

        res.put(VIAJES_LABEL, viajeService.buscarParaDador(Pageable.unpaged(), busqueda).getContent());
        res.put(TIPOREMOLQUES_LABEL, tipoRemolqueService.activosPorPaisModel(idPais));
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivosModel());
        res.put(MODELOS_LABEL, modeloService.listarActivosPorPaisModel(idPais));
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

    @PostMapping("/transportador/{idTransportador}")
    public Map<String, Object> viajePorTransportador(@PathVariable("idTransportador") String id, @RequestBody(required = false) BusquedaViajeModel busqueda, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Transportador transportador = transportadorService.buscarPorIdUsuario(id);

        if (busqueda == null) {
            busqueda = new BusquedaViajeModel();
        }

        Page<Viaje> viajes = viajeService.buscarPorCriteriosPropios(Pageable.unpaged(), busqueda, transportador);
        response.setStatus(OK.value());
        res.put(VIAJES_LABEL, viajes);
        res.put(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorList(transportador.getId()));
        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> form(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();

        if (idTransportador == null || idTransportador.isEmpty()) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, "Error en el token");
            return res;
        }

        res.put(VEHICULOS_LABEL, vehiculoService.listarActivosPorTransportadorModelList(idTransportador));
        res.put(CHOFERES_LABEL, choferService.listarActivosPorTransportadorModelList(idTransportador));
        res.put(REMOLQUES_LABEL, remolqueService.listarActivosPorTransportadorModelList(idTransportador));
        return res;
    }

    @PostMapping("/guardar/carganegativa")
    public Map<String, Object> nuevo(@RequestBody CargaNegativaModel model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        Transportador transportador = transportadorService.buscarEntidad(idTransportador);
        return guardarCargaNegativa(model, response, res, transportador);
    }

    @PostMapping("/guardar/viaje")
    public Map<String, Object> guardar(@RequestBody ViajeModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            Viaje viaje = viajeService.guardar(model);
            response.setStatus(CREATED.value());
            response.setHeader("Content-Type", "application/json;charset=UTF-8");
            res.put(CODIGO, CREATED);
            res.put("idViaje", viaje.getId());
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
            viajeService.eliminar(id);
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
        } catch (WebException we) {
            response.setStatus(NOT_FOUND.value());
            res.put(CODIGO, NOT_FOUND);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

    private Map<String, Object> guardarCargaNegativa(CargaNegativaModel model, HttpServletResponse response, Map<String, Object> res, Transportador transportador) {
        try {
            model.setIdTransportador(transportador.getId());
            Viaje v = viajeService.guardar(model);
            if (model.getId() == null || model.getId().equals("")) {
                transportador.getViajes().add(v);
                transportadorRepository.save(transportador);
            }
            res.put("id", v.getId());
            response.setStatus(OK.value());
        } catch (WebException e) {
            LOG.error(e.getMessage());
            res.put(ERROR, e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        }

        return res;
    }

    @PostMapping(value = "/actualizarestado", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> actualizarEstado(@RequestBody CoincidenciaModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Coincidencia match = matchRepository.getPorIdpedido(model.getIdPedido());
        Viaje v = match.getViaje();

        if (v == null) {
            res.put(ERROR, "Entidad no encontrada");
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        } else if (v.getEstadoViaje() == model.getViaje().getEstadoViaje() || v.getEstadoViaje().getNumber() >= model.getViaje().getEstadoViaje().getNumber()) {
            res.put(ERROR, "Estado no valido");
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        } else {
            res.put(VIAJE_LABEL, viajeService.actualizarEstado(v, model.getViaje().getEstadoViaje()));
            match.setNotificacionConformidad(EstadoNotificacion.CREADO);
            matchRepository.save(match);
            response.setStatus(OK.value());
            if (model.getViaje().getEstadoViaje() == EstadoViaje.FINALIZADO) {
                pedidoService.actualizarEstado(match.getPedido().getId(), EstadoPedido.ENTREGADO);
                Valoracion valoracionDador = new Valoracion(null, null, EstadoValoracion.CREADA, match.getPedido().getDador(), match.getTransportador().getUsuario(), match);
                Valoracion valoracionTransportador = new Valoracion(null, null, EstadoValoracion.CREADA, match.getTransportador().getUsuario(), match.getPedido().getDador(), match);
                valoracionRepository.save(valoracionDador);
                valoracionRepository.save(valoracionTransportador);
            }

            // envio de notificacion al dador cambio de estado.
            new Thread(() -> firebaseService.cambioDeEstadoDeViaje(match.getPedido().getDador().getId(), match.getPedido().getId(), model.getViaje().getEstadoViaje().getTexto())).start();
            emailService.actualizacionViaje(match.getPedido().getDador().getMail(), match.getPedido().getDador().getNombre(), match.getPedido().getId(), model.getViaje().getEstadoViaje().getTexto());
        }

        return res;
    }

}
