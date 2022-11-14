package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.CoincidenciaParaViajePropioModel;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.PedidoRepository;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.repositorios.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CoincidenciaConverter extends Convertidor<CoincidenciaModel, Coincidencia> {

    private final CoincidenciaRepository coincidenciaRepository;
    private final PedidoRepository pedidoRepository;
    private final TransportadorRepository transportadorRepository;
    private final ViajeRepository viajeRepository;
    private final ViajeConverter viajeConverter;

    public Coincidencia modeloToEntidad(CoincidenciaModel model) {
        Coincidencia match = new Coincidencia();

        if (model.getId() != null && !model.getId().isEmpty()) {
            match = coincidenciaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, match);

            if (model.getIdPedido() != null) {
                match.setPedido(pedidoRepository.getOne(model.getIdPedido()));
            }

            if (model.getIdTransportador() != null) {
                match.setTransportador(transportadorRepository.getOne(model.getIdTransportador()));
            }

            if (model.getIdViaje() != null) {
                match.setViaje(viajeRepository.getOne(model.getIdViaje()));
            }
        } catch (Exception e) {
            log.error("Error al convertir modelo <Coincidencia> a entidad: " + e.getMessage());
        }

        return match;
    }

    public CoincidenciaModel entidadToModelo(Coincidencia match) {
        CoincidenciaModel model = new CoincidenciaModel();

        try {
            BeanUtils.copyProperties(match, model);

            if (match.getComision() != null && match.getComision().getId() != null) {
                model.setIdComision(match.getComision().getId());
            }

            if (match.getPedido().getId() != null) {
                model.setIdPedido(match.getPedido().getId());
            }

            if (match.getTransportador().getId() != null) {
                model.setIdTransportador(match.getTransportador().getId());
            }

            if (match.getViaje().getId() != null) {
                model.setIdViaje(match.getViaje().getId());
                model.setViaje(viajeConverter.entidadToModelo(match.getViaje()));
            }
        } catch (Exception e) {
            log.error("Error al convertir entidad <Coincidencia> a modelo: " + e.getMessage());
        }

        return model;
    }

    public List<CoincidenciaModel> entidadesToModelos(List<Coincidencia> matches) {
        List<CoincidenciaModel> models = new ArrayList<>();

        for (Coincidencia m :
                matches) {
            models.add(entidadToModelo(m));
        }

        return models;
    }

    public List<CoincidenciaParaViajePropioModel> entidadesToModelosParaViajes(List<Coincidencia> matches) {
        List<CoincidenciaParaViajePropioModel> models = new ArrayList<>();

        for (Coincidencia m : matches) {
            CoincidenciaParaViajePropioModel match = new CoincidenciaParaViajePropioModel();
            match.setId(m.getId());
            match.setDador(m.getPedido().getDador().getNombre());
            match.setIdPedido(m.getPedido().getId());
            match.setInicioCarga(m.getPedido().getPeriodoDeCarga().getInicio());
            match.setFinalCarga(m.getPedido().getPeriodoDeCarga().getFinalizacion());
            match.setInicioDescarga(m.getPedido().getPeriodoDeDescarga().getFinalizacion());
            match.setProducto(m.getPedido().getCarga().getProducto().getNombre());
            models.add(match);
        }

        return models;
    }
}
