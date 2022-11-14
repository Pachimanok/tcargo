package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoVehiculo;
import com.tcargo.web.modelos.TipoVehiculoModel;
import com.tcargo.web.repositorios.TipoVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoVehiculoConverter extends Convertidor<TipoVehiculoModel, TipoVehiculo> {

    private final TipoVehiculoRepository tipoVehiculoRepository;

    public TipoVehiculoModel entidadToModelo(TipoVehiculo tipoVehiculo) {
        TipoVehiculoModel model = new TipoVehiculoModel();
        try {
            BeanUtils.copyProperties(tipoVehiculo, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de vehículo", e);
        }

        return model;
    }

    public TipoVehiculo modeloToEntidad(TipoVehiculoModel model) {
        TipoVehiculo tipoVehiculo = new TipoVehiculo();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoVehiculo = tipoVehiculoRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoVehiculo);
        } catch (Exception e) {
            log.error("Error al convertir el modelo tipo de vehículo en entidad", e);
        }

        return tipoVehiculo;
    }

    public List<TipoVehiculoModel> entidadesToModelos(List<TipoVehiculo> tipoVehiculos) {
        List<TipoVehiculoModel> model = new ArrayList<>();
        for (TipoVehiculo tipoVehiculo : tipoVehiculos) {
            model.add(entidadToModelo(tipoVehiculo));
        }
        return model;
    }

} 
