package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.modelos.TipoCargaModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoCargaConverter extends Convertidor<TipoCargaModel, TipoCarga> {

    private final TipoCargaRepository tipoCargaRepository;
    private final PaisRepository paisRepository;

    public TipoCargaModel entidadToModelo(TipoCarga tipoCarga) {
        TipoCargaModel model = new TipoCargaModel();

        try {
            BeanUtils.copyProperties(tipoCarga, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de carga", e);
        }

        if (tipoCarga.getPais() != null) {
            model.setIdPais(tipoCarga.getPais().getId());
        }

        return model;
    }

    public TipoCarga modeloToEntidad(TipoCargaModel model) {
        TipoCarga tipoCarga = new TipoCarga();

        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoCarga = tipoCargaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoCarga);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el tipo de carga en entidad", e);
        }

        if (model.getIdPais() != null) {
            tipoCarga.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return tipoCarga;
    }

    public List<TipoCargaModel> entidadesToModelos(List<TipoCarga> tipoCargas) {
        List<TipoCargaModel> model = new ArrayList<>();

        for (TipoCarga tipoCarga : tipoCargas) {
            model.add(entidadToModelo(tipoCarga));
        }

        return model;
    }

} 
