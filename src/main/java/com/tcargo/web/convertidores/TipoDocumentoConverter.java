package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoDocumento;
import com.tcargo.web.modelos.TipoDocumentoModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoDocumentoConverter extends Convertidor<TipoDocumentoModel, TipoDocumento> {

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PaisRepository paisRepository;

    public TipoDocumentoModel entidadToModelo(TipoDocumento tipoDocumento) {
        TipoDocumentoModel model = new TipoDocumentoModel();
        try {
            BeanUtils.copyProperties(tipoDocumento, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipoDocumento", e);
        }

        if (tipoDocumento.getPais() != null) {
            model.setIdPais(tipoDocumento.getPais().getId());
        }

        return model;
    }

    public TipoDocumento modeloToEntidad(TipoDocumentoModel model) {
        TipoDocumento tipoDocumento = new TipoDocumento();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoDocumento = tipoDocumentoRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoDocumento);
        } catch (Exception e) {
            log.error("Error al convertir el modelo tipoDocumento en entidad", e);
        }

        if (model.getIdPais() != null) {
            tipoDocumento.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return tipoDocumento;
    }

    public List<TipoDocumentoModel> entidadesToModelos(List<TipoDocumento> tipoDocumentos) {
        List<TipoDocumentoModel> model = new ArrayList<>();
        for (TipoDocumento tipoDocumento : tipoDocumentos) {
            model.add(entidadToModelo(tipoDocumento));
        }
        return model;
    }

} 
