package com.tcargo.web.controladores.rest;

import com.tcargo.web.convertidores.PedidoConverter;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PedidoCercanoModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.servicios.PedidoService;
import com.tcargo.web.servicios.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.tcargo.web.utiles.Textos.CODIGO;
import static com.tcargo.web.utiles.Textos.ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/ubicacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UbicacionRestController {

    private final PedidoService pedidoService;
    private final PedidoConverter pedidoConverter;
    private final UbicacionService ubicacionService;

    @PostMapping
    public Map<String, Object> guardar(@RequestBody List<UbicacionModel> models, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Ubicacion ubicacionDesde;
        Ubicacion ubicacionHasta;

        try {
            ubicacionDesde = ubicacionService.guardar(models.get(0));
            ubicacionHasta = ubicacionService.guardar(models.get(1));
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idUbicacionDesde", ubicacionDesde.getId());
            res.put("idUbicacionHasta", ubicacionHasta.getId());
        } catch (WebException e) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, e.getMessage());
        }

        return res;
    }

    @PostMapping("/near")
    public Set<PedidoCercanoModel> near(@RequestBody List<Ubicacion> waypoints, @RequestParam("distance") Double distance, @RequestParam("partida") String partidaViaje, @RequestParam("llegada") String llegadaViaje) {
        Set<Ubicacion> ubicacionesCercanas = new HashSet<>();
        Set<Pedido> pedidosCercanos = new HashSet<>();

        for (Ubicacion waypoint : waypoints) {
            ubicacionesCercanas.addAll(ubicacionService.buscarCercanos(waypoint, distance));
        }
        for (Ubicacion ub : ubicacionesCercanas) {
            pedidosCercanos.addAll(pedidoService.porUbicacionDesde(ub, partidaViaje, llegadaViaje));
        }

        return pedidoConverter.convertirParaPedidosCercanos(pedidosCercanos);
    }

}
