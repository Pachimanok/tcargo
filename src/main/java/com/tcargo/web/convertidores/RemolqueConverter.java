package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Remolque;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RemolqueModel;
import com.tcargo.web.repositorios.RemolqueRepository;
import com.tcargo.web.repositorios.TipoCargaRepository;
import com.tcargo.web.repositorios.TipoRemolqueRepository;
import com.tcargo.web.repositorios.TransportadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemolqueConverter extends Convertidor<RemolqueModel, Remolque> {

    private final RemolqueRepository remolqueRepository;
    private final TransportadorRepository transportadorRepository;
    private final TipoRemolqueRepository tipoRemolqueRepository;
    private final TipoCargaRepository tipoCargaRepository;

    public RemolqueModel entidadToModelo(Remolque remolque) {
        RemolqueModel model = new RemolqueModel();

        try {
            BeanUtils.copyProperties(remolque, model);
            model.setIdTransportador(remolque.getTransportador().getId());
            model.setIdTipoRemolque(remolque.getTipoRemolque().getId());
            if (remolque.getTipoCargas() != null && !remolque.getTipoCargas().isEmpty()) {
                for (TipoCarga t : remolque.getTipoCargas()) {
                    model.getIdCargas().add(t.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de remolque", e);
        }

        return model;
    }

    public Remolque modeloToEntidad(RemolqueModel model) {
        Remolque remolque = new Remolque();

        if (model.getId() != null && !model.getId().isEmpty()) {
            remolque = remolqueRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, remolque);
            remolque.setTransportador(
                    transportadorRepository.findById(model.getIdTransportador())
                            .orElseThrow(() -> new WebException("Transportador no encontrado")))
            ;
            remolque.setTipoRemolque(
                    tipoRemolqueRepository.findById(model.getIdTipoRemolque())
                            .orElseThrow(() -> new WebException("Tipo de remolque no encontrado"))
            );
            if (model.getIdCargas() != null && !model.getIdCargas().isEmpty()) {
                remolque.setTipoCargas(
                        model.getIdCargas().stream()
                                .map(id -> tipoCargaRepository.findById(id).orElse(null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de remolque en entidad", e);
        }

        return remolque;
    }

    public List<RemolqueModel> entidadesToModelos(List<Remolque> remolques) {
        List<RemolqueModel> model = new ArrayList<>();

        for (Remolque remolque : remolques) {
            model.add(entidadToModelo(remolque));
        }

        return model;
    }

} 
