package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoContenedor;
import com.tcargo.web.modelos.TipoContenedorModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoContenedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoContenedorConverter extends Convertidor<TipoContenedorModel, TipoContenedor> {

    private final TipoContenedorRepository tipoContenedorRepository;
    private final PaisRepository paisRepository;

    public TipoContenedorModel entidadToModelo(TipoContenedor tipoContenedor) {
        TipoContenedorModel model = new TipoContenedorModel();
        try {
            BeanUtils.copyProperties(tipoContenedor, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de contenedor", e);
        }

        if (tipoContenedor.getPais() != null) {
            model.setIdPais(tipoContenedor.getPais().getId());
        }

        return model;
    }

    public TipoContenedor modeloToEntidad(TipoContenedorModel model) {
        TipoContenedor tipoContenedor = new TipoContenedor();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoContenedor = tipoContenedorRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoContenedor);
        } catch (Exception e) {
            log.error("Error al convertir el modelo el tipo de contenedor en entidad", e);
        }

        if (model.getIdPais() != null) {
            tipoContenedor.setPais(paisRepository.getOne(model.getIdPais()));
        }


        return tipoContenedor;
    }

    public List<TipoContenedorModel> entidadesToModelos(List<TipoContenedor> tipoContenedors) {
        List<TipoContenedorModel> model = new ArrayList<>();
        for (TipoContenedor tipoContenedor : tipoContenedors) {
            model.add(entidadToModelo(tipoContenedor));
        }
        return model;
    }

} 
