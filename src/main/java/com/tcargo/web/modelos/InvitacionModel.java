package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import lombok.Data;

@Data
public class InvitacionModel {

    private String id;
    private EstadoNotificacion estadoNotificacion;
    private EstadoInvitacion estadoInvitacion;
    private String idChofer;
    private String nombreChofer;
    private String emailChofer;
    private String telefonoChofer;
    private String identificacion;
    private String idTransportador;
    private String nombreTransportador;

}
