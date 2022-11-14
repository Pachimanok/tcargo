package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Localidad implements Serializable {
    private static final long serialVersionUID = -8760420452725226605L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String nombre;

    private String descripcion;

    private String codigoPostal;

    @ManyToOne
    private Provincia provincia;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

}
