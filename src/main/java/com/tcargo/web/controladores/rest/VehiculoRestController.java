package com.tcargo.web.controladores.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.VehiculoModel;
import com.tcargo.web.modelos.busqueda.BusquedaVehiculoModel;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/vehiculo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VehiculoRestController {

    private final JWTService jwtService;
    private final MarcaService marcaService;
    private final ModeloService modeloService;
    private final TipoCargaService tipoCargaService;
    private final TipoVehiculoService tipoVehiculoService;
    private final VehiculoService vehiculoService;

    @PostMapping("/listado")
    public Map<String, Object> listar(@RequestBody(required = false) BusquedaVehiculoModel busqueda, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idTransportadora = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_TRANSPORTADOR_LABEL).asString();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();

        if (busqueda == null) {
            busqueda = new BusquedaVehiculoModel();
        }

        Page<Vehiculo> vehiculos = vehiculoService.buscarPorCriterios(Pageable.unpaged(), busqueda, idTransportadora);
        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(MARCAS_LABEL, marcaService.listarActivos());
        res.put(MODELOS_LABEL, modeloService.listarActivosPorPais(idPais));
        res.put(TIPOVEHICULO_LABEL, tipoVehiculoService.listarActivos());
        res.put(TIPOCARGA_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(VEHICULOS_LABEL, vehiculos);
        return res;
    }

    @GetMapping("/form")
    public Map<String, Object> formulario(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        DecodedJWT token = jwtService.decodificarToken(request.getHeader(AUTHORIZATION));
        String idPais = token.getClaim(ID_PAIS_LABEL).asString();
        String regex = token.getClaim(REGEX_PATENTE).asString();

        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        res.put(MODELOS_LABEL, modeloService.listarActivosPorPais(idPais));
        res.put(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
        res.put(REGEX_PATENTE, regex);
        return res;
    }

    @PostMapping("/guardar")
    public Map<String, Object> guardar(@RequestBody VehiculoModel model, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            Vehiculo vehiculo = vehiculoService.guardar(model);
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idVehiculo", vehiculo.getId());
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
            vehiculoService.eliminar(id);
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

}
