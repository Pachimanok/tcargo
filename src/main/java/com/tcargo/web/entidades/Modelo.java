package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Modelo implements Serializable {

    private static final long serialVersionUID = -5293694302941207678L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String nombre;
    private String descripcion;
    @ManyToOne
    private Marca marca;
    @ManyToOne
    private Pais pais;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
