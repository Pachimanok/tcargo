package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoEmbalaje;
import com.tcargo.web.modelos.TipoEmbalajeModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoEmbalajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoEmbalajeConverter extends Convertidor<TipoEmbalajeModel, TipoEmbalaje> {

    private final TipoEmbalajeRepository tipoEmbalajeRepository;
    private final PaisRepository paisRepository;

    public TipoEmbalajeModel entidadToModelo(TipoEmbalaje tipoEmbalaje) {
        TipoEmbalajeModel model = new TipoEmbalajeModel();

        try {
            BeanUtils.copyProperties(tipoEmbalaje, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de embalaje", e);
        }

        if (tipoEmbalaje.getPais() != null) {
            model.setIdPais(tipoEmbalaje.getPais().getId());
        }

        return model;
    }

    public TipoEmbalaje modeloToEntidad(TipoEmbalajeModel model) {
        TipoEmbalaje tipoEmbalaje = new TipoEmbalaje();

        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoEmbalaje = tipoEmbalajeRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoEmbalaje);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el tipo de embalaje en entidad", e);
        }

        if (model.getIdPais() != null) {
            tipoEmbalaje.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return tipoEmbalaje;
    }

    public List<TipoEmbalajeModel> entidadesToModelos(List<TipoEmbalaje> tipoEmbalajes) {
        List<TipoEmbalajeModel> model = new ArrayList<>();

        for (TipoEmbalaje tipoEmbalaje : tipoEmbalajes) {
            model.add(entidadToModelo(tipoEmbalaje));
        }

        return model;
    }

} 
