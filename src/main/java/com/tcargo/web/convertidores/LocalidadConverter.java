package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Localidad;
import com.tcargo.web.modelos.LocalidadModel;
import com.tcargo.web.repositorios.LocalidadRepository;
import com.tcargo.web.repositorios.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LocalidadConverter extends Convertidor<LocalidadModel, Localidad> {

    private final ProvinciaRepository provinciaRepository;
    private final LocalidadRepository localidadRepository;

    public LocalidadModel entidadToModelo(Localidad localidad) {
        LocalidadModel model = new LocalidadModel();
        try {
            BeanUtils.copyProperties(localidad, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de provincia", e);
        }

        if (localidad.getProvincia() != null) {
            model.setIdProvincia(localidad.getProvincia().getId());
        }

        return model;
    }

    public Localidad modeloToEntidad(LocalidadModel model) {
        Localidad localidad = new Localidad();
        if (model.getId() != null && !model.getId().isEmpty()) {
            localidad = localidadRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, localidad);
        } catch (Exception e) {
            log.error("Error al convertir el modelo la localidad en entidad", e);
        }

        if (model.getIdProvincia() != null) {
            localidad.setProvincia(provinciaRepository.getOne(model.getIdProvincia()));
        }

        return localidad;
    }

    public List<LocalidadModel> entidadesToModelos(List<Localidad> localidades) {
        List<LocalidadModel> model = new ArrayList<>();
        for (Localidad localidad : localidades) {
            model.add(entidadToModelo(localidad));
        }
        return model;
    }

} 
