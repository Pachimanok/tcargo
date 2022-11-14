package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.ViajePersonal;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ViajePersonalModel;
import com.tcargo.web.repositorios.*;
import com.tcargo.web.utiles.Fecha;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ViajePersonalConverter extends Convertidor<ViajePersonalModel, ViajePersonal> {

    private final ViajePersonalRepository viajePersonalRepository;
    private final ChoferRepository choferRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RemolqueRepository remolqueRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final UbicacionConverter ubicacionConverter;
    private final CoincidenciaConverter coincidenciaConverter;
    private final TransportadorRepository transportadorRepository;

    public ViajePersonal modeloToEntidad(ViajePersonalModel model) {
        ViajePersonal viajePersonal = new ViajePersonal();

        if (model.getId() != null) {
            viajePersonal = viajePersonalRepository.findById(model.getId()).orElse(new ViajePersonal());
        }

        try {
            BeanUtils.copyProperties(model, viajePersonal, "coincidencias");

            if (model.getCoincidencias() != null && !model.getCoincidencias().isEmpty()) {
                if (viajePersonal.getCoincidencias() == null || viajePersonal.getCoincidencias().isEmpty()) {
                    viajePersonal.setCoincidencias(Collections.emptyList());
                } else {
                    viajePersonal.getCoincidencias().removeAll(viajePersonal.getCoincidencias());
                }

                List<Coincidencia> coincidencias = model.getCoincidencias().stream()
                        .map(c -> coincidenciaRepository.findById(c.getId()).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                for (Coincidencia coincidencia : coincidencias) {
                    coincidencia.setAsignadoAViajePersonal(true);
                    viajePersonal.getCoincidencias().add(coincidenciaRepository.save(coincidencia));
                }
            }

            if (viajePersonal.getEstadoViaje() == null) {
                viajePersonal.setEstadoViaje(EstadoViaje.LISTO_PARA_CARGAR);
            }

            if (model.getIdVehiculo() != null) {
                viajePersonal.setVehiculo(vehiculoRepository.getOne(model.getIdVehiculo()));
            }

            if (model.getIdChofer() != null) {
                viajePersonal.setChofer(choferRepository.getOne(model.getIdChofer()));
            }

            if (model.getIdRemolque() != null) {
                viajePersonal.setRemolque(remolqueRepository.getOne(model.getIdRemolque()));
            }

            if (model.getFechaInicio() != null && !model.getFechaInicio().equals("")) {
                viajePersonal.setFechaInicio(Fecha.obtenerFecha(model.getFechaInicio()));
            }

            if (model.getFechaFinal() != null && !model.getFechaFinal().equals("")) {
                viajePersonal.setFechaFinal(Fecha.obtenerFecha(model.getFechaFinal()));
            }

            if (model.getIdTransportador() != null && !model.getIdTransportador().isEmpty()) {
                viajePersonal.setTransportador(transportadorRepository.findById(model.getIdTransportador()).orElseThrow(() -> new WebException("Transportador no encontrado")));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo viajePersonal a entidad", e);
        }

        return viajePersonal;
    }

    public ViajePersonalModel entidadToModelo(ViajePersonal viajePersonal) {
        ViajePersonalModel model = new ViajePersonalModel();

        try {
            if (viajePersonal.getChofer() != null) {
                model.setIdChofer(viajePersonal.getChofer().getId());
                model.setChofer(viajePersonal.getChofer().getUsuario().getNombre());
            }
            if (viajePersonal.getVehiculo() != null) {
                model.setIdVehiculo(viajePersonal.getVehiculo().getId());
                model.setVehiculo(viajePersonal.getVehiculo().getModelo().getNombre());
                model.setDominioV(viajePersonal.getVehiculo().getDominio());
            }
            if (viajePersonal.getRemolque() != null) {
                model.setIdRemolque(viajePersonal.getRemolque().getId());
                model.setRemolque(viajePersonal.getRemolque().getTipoRemolque().getCaracteristicas());
                model.setDominioR(viajePersonal.getRemolque().getDominio());
            }

            if (viajePersonal.getNuevoOrdenIds() != null && !viajePersonal.getNuevoOrdenIds().isEmpty()) {
                model.setUbicaciones(new ArrayList<>());
                model.getUbicaciones().addAll(ubicacionConverter.entidadesToModeloIds(viajePersonal.getNuevoOrdenIds()));
            }

            if (viajePersonal.getNuevoOrdenIds() != null && !viajePersonal.getNuevoOrdenIds().isEmpty()) {
                model.setNuevoOrdenIds(new ArrayList<>());
                model.getNuevoOrdenIds().addAll(viajePersonal.getNuevoOrdenIds());

            }
            if (viajePersonal.getFechaInicio() != null && viajePersonal.getFechaFinal() != null) {
                model.setFechaInicio(Fecha.formatearFechaGuiones(viajePersonal.getFechaInicio()));
                model.setFechaFinal(Fecha.formatearFechaGuiones(viajePersonal.getFechaFinal()));
            }
            model.setCoincidencias(coincidenciaConverter.entidadesToModelosParaViajes(viajePersonal.getCoincidencias()));
            model.setEstadoViaje(viajePersonal.getEstadoViaje());
            model.setId(viajePersonal.getId());
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de viajePersonal", e);
        }

        return model;
    }

    public List<ViajePersonalModel> entidadesToModelos(List<ViajePersonal> viajesPersonales) {
        List<ViajePersonalModel> models = new ArrayList<>();

        for (ViajePersonal v : viajesPersonales) {
            models.add(entidadToModelo(v));
        }

        return models;
    }

}
