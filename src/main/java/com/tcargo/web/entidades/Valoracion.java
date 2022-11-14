package com.tcargo.web.entidades;

import com.tcargo.web.enumeraciones.EstadoValoracion;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Valoracion implements Serializable {

    private static final long serialVersionUID = 6223749425189312888L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String comentarios;

    private Integer valoracion;

    private boolean conformidad;

    @Enumerated(EnumType.STRING)
    private EstadoValoracion estadoValoracion;

    @ManyToOne
    private Usuario creador;

    @ManyToOne
    private Usuario receptor;

    @ManyToOne
    private Coincidencia coincidencia;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

    public Valoracion() {
    }

    public Valoracion(String comentarios, Integer valoracion, EstadoValoracion estadoValoracion, Usuario creador, Usuario receptor, Coincidencia coincidencia) {
        this.comentarios = comentarios;
        this.valoracion = valoracion;
        this.estadoValoracion = estadoValoracion;
        this.creador = creador;
        this.receptor = receptor;
        this.coincidencia = coincidencia;
        modificacion = new Date();
    }

}
