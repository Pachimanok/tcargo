package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.VehiculoModel;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VehiculoConverter extends Convertidor<VehiculoModel, Vehiculo> {

    private final VehiculoRepository vehiculoRepository;
    private final TransportadorRepository transportadorRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final TipoCargaRepository tipoCargaRepository;
    private final ModeloRepository modeloRepository;

    public VehiculoModel entidadToModelo(Vehiculo vehiculo) {
        VehiculoModel model = new VehiculoModel();
        try {
            BeanUtils.copyProperties(vehiculo, model);
            model.setIdTransportador(vehiculo.getTransportador().getId());
            model.setIdTipoVehiculo(vehiculo.getTipoVehiculo().getId());
            model.setIdModelo(vehiculo.getModelo().getId());
            if (vehiculo.getTipoCargas() != null && !vehiculo.getTipoCargas().isEmpty()) {
                for (TipoCarga t : vehiculo.getTipoCargas()) {
                    model.getIdCargas().add(t.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de vehiculo", e);
        }

        return model;
    }

    public Vehiculo modeloToEntidad(VehiculoModel model) {
        Vehiculo vehiculo = new Vehiculo();
        if (model.getId() != null && !model.getId().isEmpty()) {
            vehiculo = vehiculoRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, vehiculo);
            vehiculo.setTransportador(
                    transportadorRepository.findById(model.getIdTransportador()).orElseThrow(() -> new WebException("Transportador no encontrado."))
            );
            vehiculo.setTipoVehiculo(
                    tipoVehiculoRepository.findById(model.getIdTipoVehiculo()).orElseThrow(() -> new WebException("Tipo de vehículo no encontrado."))
            );
            vehiculo.setModelo(
                    modeloRepository.findById(model.getIdModelo()).orElseThrow(() -> new WebException("Modelo no encontrado."))
            );
            if (model.getIdCargas() != null && !model.getIdCargas().isEmpty()) {
                if (vehiculo.getTipoCargas() == null) {
                    vehiculo.setTipoCargas(new ArrayList<>());
                }
                for (String idC : model.getIdCargas()) {
                    TipoCarga t = tipoCargaRepository.getOne(idC);
                    vehiculo.getTipoCargas().add(t);
                }
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de vehiculo en entidad", e);
        }

        return vehiculo;
    }

    public List<VehiculoModel> entidadesToModelos(List<Vehiculo> vehiculos) {
        List<VehiculoModel> model = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            model.add(entidadToModelo(vehiculo));
        }
        return model;
    }

} 
