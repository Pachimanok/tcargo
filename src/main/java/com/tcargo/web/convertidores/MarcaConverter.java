package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Marca;
import com.tcargo.web.modelos.MarcaModel;
import com.tcargo.web.repositorios.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MarcaConverter extends Convertidor<MarcaModel, Marca> {

    private final MarcaRepository marcaRepository;

    public MarcaModel entidadToModelo(Marca marca) {
        MarcaModel model = new MarcaModel();
        try {
            BeanUtils.copyProperties(marca, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de marca", e);
        }

        return model;
    }

    public Marca modeloToEntidad(MarcaModel model) {
        Marca marca = new Marca();
        if (model.getId() != null && !model.getId().isEmpty()) {
            marca = marcaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, marca);
        } catch (Exception e) {
            log.error("Error al convertir el modelo marca en entidad", e);
        }

        return marca;
    }

    public List<MarcaModel> entidadesToModelos(List<Marca> marcas) {
        List<MarcaModel> model = new ArrayList<>();
        for (Marca marca : marcas) {
            model.add(entidadToModelo(marca));
        }
        return model;
    }

} 
