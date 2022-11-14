package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoDocumentacion;
import com.tcargo.web.modelos.TipoDocumentacionModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoDocumentacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoDocumentacionConverter extends Convertidor<TipoDocumentacionModel, TipoDocumentacion> {

    private final TipoDocumentacionRepository tipoDocumentacionRepository;
    private final PaisRepository paisRepository;

    public TipoDocumentacionModel entidadToModelo(TipoDocumentacion tipoDocumentacion) {
        TipoDocumentacionModel model = new TipoDocumentacionModel();
        try {
            BeanUtils.copyProperties(tipoDocumentacion, model);
            if (tipoDocumentacion.getPais() != null) {
                model.setIdPais(tipoDocumentacion.getPais().getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de documentaión", e);
        }

        return model;
    }

    public TipoDocumentacion modeloToEntidad(TipoDocumentacionModel model) {
        TipoDocumentacion tipoDocumentacion = new TipoDocumentacion();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoDocumentacion = tipoDocumentacionRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoDocumentacion);
            if (model.getIdPais() != null) {
                tipoDocumentacion.setPais(paisRepository.getOne(model.getIdPais()));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de tipo de documentaión en entidad", e);
        }

        return tipoDocumentacion;
    }

    public List<TipoDocumentacionModel> entidadesToModelos(List<TipoDocumentacion> tipoDocumentaciones) {
        List<TipoDocumentacionModel> model = new ArrayList<>();
        for (TipoDocumentacion tipoDocumentacion : tipoDocumentaciones) {
            model.add(entidadToModelo(tipoDocumentacion));
        }
        return model;
    }

} 
