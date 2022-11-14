package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Moneda;
import com.tcargo.web.modelos.MonedaModel;
import com.tcargo.web.repositorios.MonedaRepository;
import com.tcargo.web.repositorios.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonedaConverter extends Convertidor<MonedaModel, Moneda> {

    private final MonedaRepository monedaRepository;
    private final PaisRepository paisRepository;

    public MonedaModel entidadToModelo(Moneda moneda) {
        MonedaModel model = new MonedaModel();
        try {
            BeanUtils.copyProperties(moneda, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en moneda", e);
        }

        if (moneda.getPais() != null) {
            model.setIdPais(moneda.getPais().getId());
        }

        return model;
    }

    public Moneda modeloToEntidad(MonedaModel model) {
        Moneda moneda = new Moneda();
        if (model.getId() != null && !model.getId().isEmpty()) {
            moneda = monedaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, moneda);
        } catch (Exception e) {
            log.error("Error al convertir el moneda la moneda en entidad", e);
        }

        if (model.getIdPais() != null) {
            moneda.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return moneda;
    }

    public List<MonedaModel> entidadesToMonedas(List<Moneda> monedas) {
        List<MonedaModel> model = new ArrayList<>();
        for (Moneda moneda : monedas) {
            model.add(entidadToModelo(moneda));
        }
        return model;
    }

} 
