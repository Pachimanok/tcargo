package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoRemolque;
import com.tcargo.web.modelos.TipoRemolqueModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoRemolqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoRemolqueConverter extends Convertidor<TipoRemolqueModel, TipoRemolque> {

    private final TipoRemolqueRepository tipoRemolqueRepository;
    private final PaisRepository paisRepository;

    public TipoRemolqueModel entidadToModelo(TipoRemolque tipoRemolque) {
        TipoRemolqueModel model = new TipoRemolqueModel();
        try {
            BeanUtils.copyProperties(tipoRemolque, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de remolque", e);
        }

        if (tipoRemolque.getPais() != null) {
            model.setIdPais(tipoRemolque.getPais().getId());
        }

        return model;
    }

    public TipoRemolque modeloToEntidad(TipoRemolqueModel model) {
        TipoRemolque tipoRemolque = new TipoRemolque();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoRemolque = tipoRemolqueRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoRemolque);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el tipo de remolque en entidad", e);
        }

        if (model.getIdPais() != null) {
            tipoRemolque.setPais(paisRepository.getOne(model.getIdPais()));
        }


        return tipoRemolque;
    }

    public List<TipoRemolqueModel> entidadesToModelos(List<TipoRemolque> tipoRemolques) {
        List<TipoRemolqueModel> model = new ArrayList<>();
        for (TipoRemolque tipoRemolque : tipoRemolques) {
            model.add(entidadToModelo(tipoRemolque));
        }
        return model;
    }

} 
