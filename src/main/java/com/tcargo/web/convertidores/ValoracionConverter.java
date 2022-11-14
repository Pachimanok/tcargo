package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Valoracion;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ValoracionConverter extends Convertidor<ValoracionModel, Valoracion> {

    private final UsuarioRepository usuarioRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final CoincidenciaConverter coincidenciaConverter;

    @Override
    public Valoracion modeloToEntidad(ValoracionModel model) {
        Valoracion v = new Valoracion();

        try {
            BeanUtils.copyProperties(model, v);
            if (model.getIdCreador() != null && !model.getIdCreador().isEmpty()) {
                v.setCreador(usuarioRepository.getOne(model.getIdCreador()));
            }
            if (model.getIdReceptor() != null && !model.getIdReceptor().isEmpty()) {
                v.setReceptor(usuarioRepository.getOne(model.getIdReceptor()));
            }
            if (model.getCoincidencia() != null) {
                v.setCoincidencia(coincidenciaRepository.getOne(model.getCoincidencia().getId()));
            }
        } catch (Exception e) {
            log.error("Error al convertir modelo Valoracion a entidad: " + e.getMessage());
        }

        return v;
    }

    @Override
    public ValoracionModel entidadToModelo(Valoracion v) {
        ValoracionModel model = new ValoracionModel();

        try {
            BeanUtils.copyProperties(v, model);
            if (v.getCreador() != null) {
                model.setIdCreador(v.getCreador().getId());
                model.setNombreCreador(v.getCreador().getNombre());
            }
            if (v.getReceptor() != null) {
                model.setIdReceptor(v.getReceptor().getId());
                model.setNombreReceptor(v.getReceptor().getNombre());
            }
            if (v.getCoincidencia() != null) {
                model.setCoincidencia(coincidenciaConverter.entidadToModelo(v.getCoincidencia()));
            }
        } catch (Exception e) {
            log.error("Error al convertir entidad Valoracion a modelo: " + e.getMessage());
        }

        return model;
    }

    public List<ValoracionModel> entidadesToModelo(List<Valoracion> valoraciones) {
        List<ValoracionModel> models = new ArrayList<>();
        for (Valoracion v : valoraciones) {
            models.add(entidadToModelo(v));
        }
        return models;
    }

}
