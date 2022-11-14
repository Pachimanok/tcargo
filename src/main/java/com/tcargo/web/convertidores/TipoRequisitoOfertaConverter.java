package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.modelos.TipoRequisitoOfertaModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoRequisitoOfertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoRequisitoOfertaConverter extends Convertidor<TipoRequisitoOfertaModel, TipoRequisitoOferta> {

    private final TipoRequisitoOfertaRepository tipoRequisitoOfertaRepository;
    private final PaisRepository paisRepository;

    public TipoRequisitoOfertaModel entidadToModelo(TipoRequisitoOferta tipoRequisitoOferta) {
        TipoRequisitoOfertaModel model = new TipoRequisitoOfertaModel();
        try {
            BeanUtils.copyProperties(tipoRequisitoOferta, model);
            if (tipoRequisitoOferta.getPais() != null) {
                model.setIdPais(tipoRequisitoOferta.getPais().getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de documentación", e);
        }

        return model;
    }

    public TipoRequisitoOferta modeloToEntidad(TipoRequisitoOfertaModel model) {
        TipoRequisitoOferta tipoRequisitoOferta = new TipoRequisitoOferta();
        if (model.getId() != null && !model.getId().isEmpty()) {
            tipoRequisitoOferta = tipoRequisitoOfertaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, tipoRequisitoOferta);
            if (model.getIdPais() != null) {
                tipoRequisitoOferta.setPais(paisRepository.getOne(model.getIdPais()));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de tipo de documentación en entidad", e);
        }

        return tipoRequisitoOferta;
    }

    public List<TipoRequisitoOfertaModel> entidadesToModelos(List<TipoRequisitoOferta> tipoRequisitosOfertas) {
        List<TipoRequisitoOfertaModel> model = new ArrayList<>();
        for (TipoRequisitoOferta tipoRequisitoOferta : tipoRequisitosOfertas) {
            model.add(entidadToModelo(tipoRequisitoOferta));
        }
        return model;
    }

} 
