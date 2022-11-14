package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Requisito;
import com.tcargo.web.modelos.RequisitoModel;
import com.tcargo.web.repositorios.RequisitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequisitoConverter extends Convertidor<RequisitoModel, Requisito> {

    private final RequisitoRepository requisitoRepository;

    public RequisitoModel entidadToModelo(Requisito requisito) {
        RequisitoModel model = new RequisitoModel();
        try {
            BeanUtils.copyProperties(requisito, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de parequisito", e);
        }

        return model;
    }

    public Requisito modeloToEntidad(RequisitoModel model) {
        Requisito requisito = new Requisito();
        if (model.getId() != null && !model.getId().isEmpty()) {
            requisito = requisitoRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, requisito);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el requisito en entidad", e);
        }

        return requisito;
    }

    public List<RequisitoModel> entidadesToModelos(List<Requisito> requisitos) {
        List<RequisitoModel> model = new ArrayList<>();
        for (Requisito requisito : requisitos) {
            model.add(entidadToModelo(requisito));
        }
        return model;
    }

} 
