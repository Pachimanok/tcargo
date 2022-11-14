package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.PeriodoDeCarga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.servicios.PeriodoDeCargaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.CODIGO;
import static com.tcargo.web.utiles.Textos.ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/periodo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PeriodoDeCargaRestController {

    private final PeriodoDeCargaService periodoDeCargaService;

    @PostMapping
    public Map<String, Object> guardar(@RequestBody List<PeriodoDeCarga> periodos, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        PeriodoDeCarga periodoDeCarga;
        PeriodoDeCarga periodoDeDescarga;

        try {
            periodoDeCarga = periodoDeCargaService.guardar(periodos.get(0));
            periodoDeDescarga = periodoDeCargaService.guardar(periodos.get(1));
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idPeriodoDeCarga", periodoDeCarga.getId());
            res.put("idPeriodoDeDescarga", periodoDeDescarga.getId());
        } catch (WebException e) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, e.getMessage());
        }

        return res;
    }

}
