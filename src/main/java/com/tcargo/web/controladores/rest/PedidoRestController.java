package com.tcargo.web.controladores.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.*;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.PedidoRestModel;
import com.tcargo.web.modelos.busqueda.BusquedaPedidoModel;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.tcargo.web.utiles.Textos.*;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/pedido")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PedidoRestController {

    private final CoincidenciaService coincidenciaService;
    private final ContraOfertaService contraOfertaService;
    private final DadorDeCargaService dadorDeCargaService;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final MonedaService monedaService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final TipoCargaService tipoCargaService;
    private final TipoContenedorService tipoContenedorService;
    private final TipoEmbalajeService tipoEmbalajeService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TipoRequisitoOfertaService tipoRequisitoOfertaService;
    private final TipoVehiculoService tipoVehiculoService;
    private final TransportadorService transportadorService;

    @PostMapping("/dador")
    public Map<String, Object> listadoDador(Pageable pageable, @RequestBody(required = false) BusquedaPedidoModel busqueda, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();
        String idUsuario = token.getClaim("id").asString();
        DadorDeCarga dador = dadorDeCargaService.buscarPorIdUsuario(idUsuario);

        if (busqueda == null) {
            busqueda = new BusquedaPedidoModel();
        }

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(Sort.by("p.estado_pedido").ascending().and(Sort.by("pc.inicio").descending())));

        if (dador != null) {
            res.put(ESTADOS_LABEL, EstadoPedido.values());
            res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
            res.put(TIPOCARGA_LABEL, tipoCargaService.activosPorPaisModel(idPais));
            res.put(PEDIDOS_LABEL, pedidoService.buscarPorCriteriosParaDador(pageable, busqueda, dador.getUsuario(), true));
            res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
            res.put(CODIGO, OK);
            response.setStatus(OK.value());
            return res;
        }

        response.setStatus(NOT_FOUND.value());
        res.put(CODIGO, NOT_FOUND);
        res.put(ERROR, "Dador no encontrado");
        return res;
    }

    @PostMapping("/transportador/{id}")
    public Map<String, Object> listadoTransportador(Pageable pageable, @PathVariable("id") String id, @RequestBody(required = false) BusquedaPedidoModel busqueda,
                                                    HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        Transportador transportador = transportadorService.buscarPorIdUsuario(id);

        if (busqueda == null) {
            busqueda = new BusquedaPedidoModel();
        }

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(Sort.by("p.estado_pedido").ascending().and(Sort.by("pc.inicio").descending())));

        if (transportador != null) {
            List<ContraOferta> contraOfertas = contraOfertaService.listarPorTransportadorIdList(transportador);
            res.put(PEDIDOS_LABEL, pedidoService.buscarPorCriteriosParaTransportador(pageable, busqueda, contraOfertas, false));
            res.put(ESTADOS_LABEL, EstadoPedido.values());
            res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
            res.put(TIPOCARGA_LABEL, tipoCargaService.activosPorPaisModel(idPais));
            res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
            res.put(CODIGO, OK);
            response.setStatus(OK.value());
            return res;
        }

        response.setStatus(NOT_FOUND.value());
        res.put(CODIGO, NOT_FOUND);
        res.put(ERROR, "Transportador no encontrado");
        return res;
    }

    @GetMapping("/carga/{rol}/{id}")
    public Map<String, Object> listadoPedidosGrupo(Pageable pageable, @PathVariable("rol") String rol, @PathVariable("id") String id,
                                                   HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        switch (rol.toLowerCase()) {
            case TRANSPORTADOR_LABEL:
                res.put(PEDIDOS_LABEL, pedidoService.buscarPorIdCargaPageEliminarMatcheados(pageable, id));
                break;
            case DADORDECARGA_LABEL:
                res.put(PEDIDOS_LABEL, pedidoService.buscarPorIdCarga(id));
                break;
            default:
                response.setStatus(BAD_REQUEST.value());
                res.put(CODIGO, BAD_REQUEST);
                res.put(ERROR, "Rol no valido");
                return res;
        }

        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        return res;
    }

    @GetMapping(value = "/eliminar-grupo", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String eliminarGrupo(@RequestParam(value = "cargaId", required = false) String idCarga, ModelMap model) {

        try {
            List<Pedido> pedidos = pedidoService.buscarPorIdCarga(idCarga);
            pedidoService.eliminarGrupo(pedidos);
            model.addAttribute("success", "Grupo eliminado con exito");
            return "OK";
        } catch (Exception e) {
            model.addAttribute(ERROR, "Ocurrió un error inesperado al intentar eliminar el pedido.");
            return "ERROR";
        }
    }

    @PostMapping("/mis-fletes")
    public Map<String, Object> misFletes(Pageable pageable, @RequestBody(required = false) BusquedaPedidoModel busqueda,
                                         HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportador = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        List<Coincidencia> matches = coincidenciaService.buscarActivosPorIdTransportadorList(null, idTransportador, null);
        Set<Long> idsPedidos = matches.stream().map(m -> m.getPedido().getId()).collect(toSet());

        if (busqueda == null) {
            busqueda = new BusquedaPedidoModel();
        }

        response.setStatus(OK.value());
        res.put(CODIGO, OK);

        if (idsPedidos.isEmpty()) {
            res.put(PEDIDOS_LABEL, new PageImpl<>(new ArrayList<>(), pageable, 0));
        } else {
            res.put(PEDIDOS_LABEL, pedidoService.buscarMatcheadosPorCriterios(pageable, busqueda, new ArrayList<>(idsPedidos)));
        }

        res.put(ESTADOS_LABEL, EstadoPedido.values());
        res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        res.put(TIPOCARGA_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        return res;
    }

    @GetMapping("/ver/{id}")
    public Map<String, Object> pedido(@PathVariable("id") Long id, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        Pedido pedido = pedidoService.buscarPorId(id);

        if (pedido != null) {
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
            res.put(PEDIDO_LABEL, pedido);
            return res;
        }

        response.setStatus(BAD_REQUEST.value());
        res.put(CODIGO, BAD_REQUEST);
        res.put(ERROR, "id no valido");
        return res;
    }

    @PostMapping("/pedidos-ofertados")
    public Map<String, Object> pedidosOfertados(Pageable pageable, @RequestBody(required = false) BusquedaPedidoModel busqueda,
                                                HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportadora = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        Transportador transportador = transportadorService.buscarEntidad(idTransportadora);
        List<ContraOferta> contraOfertas = contraOfertaService.listarPorTransportadorIdList(transportador);
        List<Coincidencia> matches = coincidenciaService.buscarActivosPorIdTransportadorList(null, idTransportadora, null);
        Set<Long> idsPedidos = contraOfertas.stream().filter(c -> c.getEstado() == EstadoContraOferta.PENDIENTE).map(c -> c.getPedido().getId()).collect(toSet());
        idsPedidos.removeAll(matches.stream().map(m -> m.getPedido().getId()).collect(toSet()));

        if (busqueda == null) {
            busqueda = new BusquedaPedidoModel();
        }

        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(ESTADOS_LABEL, EstadoPedido.values());
        res.put(TIPOCARGA_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        res.put(PEDIDOS_LABEL, pedidoService.buscarOfertadosPorCriterios(pageable, busqueda, new ArrayList<>(idsPedidos)));
        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> form(@RequestParam(value = "idViaje", required = false) String idViaje, HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();

        if (idViaje != null) {
            res.put("mailTransportador", transportadorService.porIdViaje(idViaje).getUsuario().getMail());
        } else {
            res.put("mailTransportador", null);
        }

        res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        res.put("tipoInternacional", Arrays.asList(TipoDeViaje.EXPORTACION, TipoDeViaje.IMPORTACION));
        res.put(TIPOCONTENEDOR_LABEL, tipoContenedorService.listarActivosModel());
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(TIPOPESOS_LABEL, TipoPeso.values());
        res.put(TIPOEMBALAJE_LABEL, tipoEmbalajeService.activosPorPaisModel(idPais));
        res.put(TIPOREMOLQUE_LABEL, tipoRemolqueService.activosPorPaisModel(idPais));
        res.put(TIPOVEHICULO_LABEL, tipoVehiculoService.listarActivos());
        res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        res.put(MONEDAS_LABEL, monedaService.listarActivos());
        res.put(REQUISITOS_LABEL, tipoRequisitoOfertaService.listarActivos());

        return res;
    }

    @PostMapping
    public Map<String, Object> guardar(@RequestBody PedidoRestModel pedido, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idUsuario = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        try {
            pedido.setIdDador(idUsuario);
            Long idPedido = pedidoService.guardarDesdeMobile(pedido);
            if (pedido.getMailTransportador() != null) {
                emailService.cargaNegativaSeleccionada(pedido.getMailTransportador(), "", idPedido);
            }
            res.put(CODIGO, CREATED);
            res.put("idPedido", "Pedido " + idPedido + " creado");
            return res;
        } catch (WebException e) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, e.getMessage());
            return res;
        }
    }

    @PostMapping("/chofer/fletes")
    public Map<String, Object> verFletesChofer(@PageableDefault(size = 20, sort = "pc.inicio", direction = Sort.Direction.DESC) Pageable pageable,
                                               @RequestBody(required = false) BusquedaPedidoModel busqueda, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idChofer = token.getClaim("idChofer").asString();
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();
        List<CoincidenciaModel> matches = coincidenciaService.buscarPorChoferEnViaje(idChofer);
        Set<Long> idPedidos = matches.stream().map(CoincidenciaModel::getIdPedido).collect(toSet());

        if (busqueda == null) {
            busqueda = new BusquedaPedidoModel();
        }

        if (!idPedidos.isEmpty()) {
            res.put(PEDIDOS_LABEL, pedidoService.armarPedidoChofer(pedidoService.buscarMatcheadosPorCriterios(pageable, busqueda, new ArrayList<>(idPedidos)).getContent()));
        } else {
            res.put(PEDIDOS_LABEL, new ArrayList<>());
        }

        res.put(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        res.put("estadoPedidos", Arrays.asList(EstadoPedido.values()));
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        res.put("viajeEstados", EstadoViaje.getEstados());
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

}
