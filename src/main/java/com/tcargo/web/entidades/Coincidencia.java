package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import com.tcargo.web.enumeraciones.EstadoNotificacion;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Coincidencia implements Serializable {
    private static final long serialVersionUID = 8118137275653132334L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private Double costo;
    private String detalle;
    private Boolean asignadoAViajePersonal;
    private Boolean aprobado;
    private Boolean finalizado;
    @Enumerated(EnumType.STRING)
    private EstadoNotificacion notificacion;
    @Enumerated(EnumType.STRING)
    private EstadoNotificacion notificacionConformidad;
    
    @ManyToOne
    private Comision comision;

    @ManyToOne
    private Viaje viaje;

    @ManyToOne
    private Transportador transportador;

    @ManyToOne
    private Pedido pedido;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;
}
