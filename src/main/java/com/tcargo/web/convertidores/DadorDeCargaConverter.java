package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.modelos.DadorDeCargaModel;
import com.tcargo.web.repositorios.DadorDeCargaRepository;
import com.tcargo.web.repositorios.UbicacionRepository;
import com.tcargo.web.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DadorDeCargaConverter extends Convertidor<DadorDeCargaModel, DadorDeCarga> {

    private final DadorDeCargaRepository dadorDeCargaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public DadorDeCargaModel entidadToModelo(DadorDeCarga dadorDeCarga) {
        DadorDeCargaModel model = new DadorDeCargaModel();

        try {
            BeanUtils.copyProperties(dadorDeCarga, model);
            if (dadorDeCarga.getUbicacion() != null) {
                model.setIdUbicacion(ubicacionRepository.getOne(dadorDeCarga.getUbicacion().getId()).getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de dadorDeCarga", e);
        }

        return model;
    }

    public DadorDeCarga modeloToEntidad(DadorDeCargaModel model) {
        DadorDeCarga dadorDeCarga = new DadorDeCarga();

        if (model.getId() != null && !model.getId().isEmpty()) {
            dadorDeCarga = dadorDeCargaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, dadorDeCarga);
            dadorDeCarga.setUsuario(usuarioRepository.buscarPorId(model.getIdUsuario()));
            dadorDeCarga.setUbicacion(ubicacionRepository.getOne(model.getIdUbicacion()));

        } catch (Exception e) {
            log.error("Error al convertir el modelo de dadorDeCarga en entidad", e);
        }

        return dadorDeCarga;
    }

    public DadorDeCarga modeloToEntidadSinUbicacion(DadorDeCargaModel model) {
        DadorDeCarga dadorDeCarga = new DadorDeCarga();

        if (model.getId() != null && !model.getId().isEmpty()) {
            dadorDeCarga = dadorDeCargaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, dadorDeCarga);
            dadorDeCarga.setUsuario(usuarioRepository.buscarPorId(model.getIdUsuario()));

        } catch (Exception e) {
            log.error("Error al convertir el modelo de dadorDeCarga en entidad", e);
        }

        return dadorDeCarga;
    }

    public List<DadorDeCargaModel> entidadesToModelos(List<DadorDeCarga> dadorDeCargaes) {
        List<DadorDeCargaModel> model = new ArrayList<>();

        for (DadorDeCarga dadorDeCarga : dadorDeCargaes) {
            model.add(entidadToModelo(dadorDeCarga));
        }

        return model;
    }


} 
