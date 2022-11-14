package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Invitacion;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.modelos.InvitacionModel;
import com.tcargo.web.repositorios.InvitacionRepository;
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
public class InvitacionConverter extends Convertidor<InvitacionModel, Invitacion> {

    private final InvitacionRepository invitacionRepository;
    private final TransportadorRepository transportadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public InvitacionModel entidadToModelo(Invitacion invitacion) {
        InvitacionModel model = new InvitacionModel();
        try {
            BeanUtils.copyProperties(invitacion, model);

            if (invitacion.getChofer() != null) {
                model.setIdChofer(invitacion.getChofer().getId());
                model.setNombreChofer(invitacion.getChofer().getNombre());
                if (invitacion.getChofer().getMail() != null && !invitacion.getChofer().getMail().isEmpty()) {
                    model.setEmailChofer(invitacion.getChofer().getMail());
                }
                if (invitacion.getChofer().getTelefono() != null && !invitacion.getChofer().getTelefono().isEmpty()) {
                    model.setTelefonoChofer(invitacion.getChofer().getTelefono());
                }
            }
            if (invitacion.getTransportador() != null) {
                model.setIdTransportador(invitacion.getTransportador().getId());
                model.setNombreTransportador(invitacion.getTransportador().getNombre());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de tipo de remolque", e);
        }

        return model;
    }

    @Override
    public Invitacion modeloToEntidad(InvitacionModel model) {
        Invitacion invitacion = new Invitacion();

        if (model.getId() != null && !model.getId().isEmpty()) {
            invitacion = invitacionRepository.findById(model.getId()).orElse(new Invitacion());
        }

        try {
            BeanUtils.copyProperties(model, invitacion);

            if (model.getIdChofer() != null && !model.getIdChofer().isEmpty()) {
                Usuario c = usuarioRepository.buscarPorId(model.getIdChofer());
                invitacion.setChofer(c);
            }
            if (model.getIdTransportador() != null && !model.getIdTransportador().isEmpty()) {
                Transportador t = transportadoRepository.findById(model.getIdTransportador()).orElse(null);
                invitacion.setTransportador(t);
            }
            if (model.getEstadoInvitacion() == null) {
                invitacion.setEstadoInvitacion(EstadoInvitacion.ENVIADA);
            }
            if (model.getEstadoNotificacion() == null) {
                invitacion.setEstadoNotificacion(EstadoNotificacion.CREADO);
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo el tipo de remolque en entidad", e);
        }

        return invitacion;
    }

    public List<InvitacionModel> entidadesToModelos(List<Invitacion> invitaciones) {
        List<InvitacionModel> model = new ArrayList<>();
        for (Invitacion invitacion : invitaciones) {
            model.add(entidadToModelo(invitacion));
        }
        return model;
    }

} 
