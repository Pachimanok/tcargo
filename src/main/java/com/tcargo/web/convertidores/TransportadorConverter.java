package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TransportadorModel;
import com.tcargo.web.repositorios.TransportadorRepository;
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
public class TransportadorConverter extends Convertidor<TransportadorModel, Transportador> {

    private final TransportadorRepository transportadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;

    public TransportadorModel entidadToModelo(Transportador transportador) {
        TransportadorModel model = new TransportadorModel();
        try {
            BeanUtils.copyProperties(transportador, model);
            if (transportador.getUbicacion() != null) {
                model.setIdUbicacion(
                        ubicacionRepository.findById(transportador.getUbicacion().getId())
                                .orElseThrow(() -> new WebException("Ubicación no encontrada")).getId()
                );
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de transportador", e);
        }


        return model;
    }

    public Transportador modeloToEntidad(TransportadorModel model) {
        Transportador transportador = new Transportador();
        if (model.getId() != null && !model.getId().isEmpty()) {
            transportador = transportadorRepository.getOne(model.getId());
        }
        if (model.getIdUsuario() != null && !model.getIdUsuario().isEmpty()) {
            transportador.setUsuario(usuarioRepository.getOne(model.getIdUsuario()));
        }

        try {
            BeanUtils.copyProperties(model, transportador);
        } catch (Exception e) {
            log.error("Error al convertir el modelo de transportador en entidad", e);
        }


        return transportador;
    }

    public List<TransportadorModel> entidadesToModelos(List<Transportador> transportadores) {
        List<TransportadorModel> model = new ArrayList<>();
        for (Transportador transportador : transportadores) {
            model.add(entidadToModelo(transportador));
        }
        return model;
    }


} 
