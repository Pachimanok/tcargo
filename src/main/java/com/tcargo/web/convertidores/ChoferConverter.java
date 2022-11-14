package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.modelos.ChoferModel;
import com.tcargo.web.repositorios.ChoferRepository;
import com.tcargo.web.repositorios.TipoDocumentoRepository;
import com.tcargo.web.repositorios.TransportadorRepository;
import com.tcargo.web.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ChoferConverter extends Convertidor<ChoferModel, Chofer> {

    private final ChoferRepository choferRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final TransportadorRepository transportadorRepository;
    private final UsuarioRepository usuarioRepository;

    public ChoferModel entidadToModelo(Chofer chofer) {
        ChoferModel model = new ChoferModel();

        try {
            BeanUtils.copyProperties(chofer, model);
            if (chofer.getTransportador() != null && chofer.getTransportador().getId() != null && !chofer.getTransportador().getId().isEmpty()) {
                model.setIdTransportador(chofer.getTransportador().getId());
            }
            model.getUsuario().setId(chofer.getUsuario().getId());
            model.getUsuario().setNombre(chofer.getUsuario().getNombre());
            model.getUsuario().setTelefono(chofer.getUsuario().getTelefono());
            model.getUsuario().setMail(chofer.getUsuario().getMail());
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de chofer", e);
        }

        return model;
    }

    public Chofer modeloToEntidad(ChoferModel model) {
        Chofer chofer = new Chofer();

        if (model.getId() != null && !model.getId().isEmpty()) {
            chofer = choferRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, chofer);
            chofer.setUsuario(usuarioRepository.buscarPorId(model.getUsuario().getId()));
            if (model.getIdTransportador() != null && !model.getIdTransportador().isEmpty()) {
                chofer.setTransportador(transportadorRepository.getOne(model.getIdTransportador()));
            }
            if (model.getIdTipoDocumento() != null && !model.getIdTipoDocumento().isEmpty()) {
                chofer.setTipoDocumento(tipoDocumentoRepository.getOne(model.getIdTipoDocumento()));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de chofer en entidad", e);
        }

        return chofer;
    }

    public List<ChoferModel> entidadesToModelos(List<Chofer> chofers) {
        List<ChoferModel> model = new ArrayList<>();

        for (Chofer chofer : chofers) {
            model.add(entidadToModelo(chofer));
        }

        return model;
    }


} 
