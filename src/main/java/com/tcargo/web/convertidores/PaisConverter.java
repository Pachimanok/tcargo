package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.modelos.PaisModel;
import com.tcargo.web.repositorios.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaisConverter extends Convertidor<PaisModel, Pais> {

    private final PaisRepository paisRepository;

    public PaisModel entidadToModelo(Pais pais) {
        PaisModel model = new PaisModel();
        try {
            BeanUtils.copyProperties(pais, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de país", e);
        }

        return model;
    }

    public Pais modeloToEntidad(PaisModel model) {
        Pais pais = new Pais();
        if (model.getId() != null && !model.getId().isEmpty()) {
            pais = paisRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, pais);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el país en entidad", e);
        }

        return pais;
    }

    public List<PaisModel> entidadesToModelos(List<Pais> paiss) {
        List<PaisModel> model = new ArrayList<>();
        for (Pais pais : paiss) {
            model.add(entidadToModelo(pais));
        }
        return model;
    }

} 
