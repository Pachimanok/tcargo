package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.InvitacionConverter;
import com.tcargo.web.entidades.Invitacion;
import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.InvitacionModel;
import com.tcargo.web.repositorios.InvitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InvitacionService {

    private final InvitacionRepository invitacionRepository;
    private final InvitacionConverter invitacionConverter;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Invitacion guardar(InvitacionModel model) throws WebException {
        Invitacion invitacion = invitacionConverter.modeloToEntidad(model);

        if (invitacion.getEliminado() != null) {
            throw new WebException("La invitacion que intenta aceptar o rechazar se encuentra dada de baja.");
        }

        if (invitacion.getChofer() == null) {
            throw new WebException("La invitacion debe tener un chofer.");
        }

        if (invitacion.getTransportador() == null) {
            throw new WebException("La invitacion debe tener una transportadora.");
        }

        invitacion.setModificacion(new Date());

        return invitacionRepository.save(invitacion);
    }

    public List<Invitacion> buscarEstadoInvTransportadorAndEstadoNotif(EstadoInvitacion inv, String idTransportador, EstadoNotificacion notif) {
        return invitacionRepository.buscarPorEstadoInvAndTransportadorAndEstadoNotif(inv, idTransportador, notif);
    }

    public InvitacionModel buscarModel(String id) {
        return invitacionConverter.entidadToModelo(invitacionRepository.findById(id).orElse(null));
    }

}
