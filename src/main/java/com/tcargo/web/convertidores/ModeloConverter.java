package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Modelo;
import com.tcargo.web.modelos.ModeloModel;
import com.tcargo.web.repositorios.MarcaRepository;
import com.tcargo.web.repositorios.ModeloRepository;
import com.tcargo.web.repositorios.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ModeloConverter extends Convertidor<ModeloModel, Modelo> {

    private final ModeloRepository modeloRepository;
    private final MarcaRepository marcaRepository;
    private final PaisRepository paisRepository;

    public ModeloModel entidadToModelo(Modelo modelo) {
        ModeloModel model = new ModeloModel();
        try {
            BeanUtils.copyProperties(modelo, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo", e);
        }

        if (modelo.getMarca() != null) {
            model.setIdMarca(modelo.getMarca().getId());
        }

        if (modelo.getPais() != null) {
            model.setIdPais(modelo.getPais().getId());
        }

        return model;
    }

    public Modelo modeloToEntidad(ModeloModel model) {
        Modelo modelo = new Modelo();
        if (model.getId() != null && !model.getId().isEmpty()) {
            modelo = modeloRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, modelo);
        } catch (Exception e) {
            log.error("Error al convertir el modelo la modelo en entidad", e);
        }

        if (model.getIdMarca() != null) {
            modelo.setMarca(marcaRepository.getOne(model.getIdMarca()));
        }

        if (model.getIdPais() != null) {
            modelo.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return modelo;
    }

    public List<ModeloModel> entidadesToModelos(List<Modelo> modelos) {
        List<ModeloModel> model = new ArrayList<>();
        for (Modelo modelo : modelos) {
            model.add(entidadToModelo(modelo));
        }
        return model;
    }

} 
