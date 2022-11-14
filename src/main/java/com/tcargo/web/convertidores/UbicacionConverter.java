package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UbicacionConverter extends Convertidor<UbicacionModel, Ubicacion> {

    private final PaisRepository paisRepository;
    private final UbicacionRepository ubicacionRepository;

    public UbicacionModel entidadToModelo(Ubicacion ubicacion) {
        UbicacionModel model = new UbicacionModel();

        try {
            BeanUtils.copyProperties(ubicacion, model);
            if (ubicacion.getPais() != null) {
                model.setIdPais(ubicacion.getPais().getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de ubicacion", e);
        }

        return model;
    }

    public Ubicacion modeloToEntidad(UbicacionModel model) {
        Ubicacion ubicacion = new Ubicacion();

        if (model.getId() != null && !model.getId().isEmpty()) {
            ubicacion = ubicacionRepository.findById(model.getId()).orElse(new Ubicacion());
        }

        try {
            BeanUtils.copyProperties(model, ubicacion);

            if (model.getIdPais() != null) {
                ubicacion.setPais(paisRepository.getOne(model.getIdPais()));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo la ubicacion en entidad", e);
        }

        return ubicacion;
    }

    public List<UbicacionModel> entidadesToModelos(List<Ubicacion> ubicaciones) {
        List<UbicacionModel> model = new ArrayList<>();

        for (Ubicacion ubicacion : ubicaciones) {
            model.add(entidadToModelo(ubicacion));
        }

        return model;
    }

    public List<UbicacionModel> entidadesToModeloIds(List<String> ids) {
        return ids.stream()
                .map(id -> ubicacionRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(this::entidadToModelo)
                .collect(Collectors.toList());
    }

}
