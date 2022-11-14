package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Provincia;
import com.tcargo.web.modelos.ProvinciaModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProvinciaConverter extends Convertidor<ProvinciaModel, Provincia> {

    private final ProvinciaRepository provinciaRepository;
    private final PaisRepository paisRepository;

    public ProvinciaModel entidadToModelo(Provincia provincia) {
        ProvinciaModel model = new ProvinciaModel();
        try {
            BeanUtils.copyProperties(provincia, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de provincia", e);
        }

        if (provincia.getPais() != null) {
            model.setIdPais(provincia.getPais().getId());
        }

        return model;
    }

    public Provincia modeloToEntidad(ProvinciaModel model) {
        Provincia provincia = new Provincia();
        if (model.getId() != null && !model.getId().isEmpty()) {
            provincia = provinciaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, provincia);
        } catch (Exception e) {
            log.error("Error al convertir el modelo la provincia en entidad", e);
        }

        if (model.getIdPais() != null) {
            provincia.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return provincia;
    }

    public List<ProvinciaModel> entidadesToModelos(List<Provincia> provincias) {
        List<ProvinciaModel> model = new ArrayList<>();
        for (Provincia provincia : provincias) {
            model.add(entidadToModelo(provincia));
        }
        return model;
    }

} 
