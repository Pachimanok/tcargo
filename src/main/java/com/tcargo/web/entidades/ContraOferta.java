package com.tcargo.web.entidades;

import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class ContraOferta implements Serializable {

    private static final long serialVersionUID = 881280540260000190L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private Boolean seguroCarga;
    private Boolean incluyeBajadaAPiso;
    private Boolean aduana;
    private Boolean carteleriaCargaPeligrosa;
    private Boolean incluyeIva;
    private Double valor;
    private Integer diasLibres;
    private String comentarios;
    private Boolean isFinal;

    @Enumerated(EnumType.STRING)
    private EstadoContraOferta estado;
    private EstadoNotificacion notificacion;

    @ManyToMany
    private List<TipoRequisitoOferta> requisitosDeOferta = new ArrayList<>();

    @ManyToOne
    private Pedido pedido;

    @ManyToOne
    private Usuario dador;

    @ManyToOne
    private Transportador transportador;

    @ManyToOne
    private Usuario creador;

    @ManyToOne
    private Vehiculo vehiculo;

    @ManyToOne
    private Remolque remolque;

    @ManyToOne
    private Moneda moneda;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

}
