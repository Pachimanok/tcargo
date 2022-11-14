package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class TipoCarga implements Serializable {

    private static final long serialVersionUID = -7403777315916870332L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String caracteristicas;
    private String descripcion;
    private boolean isPalet;

    @ManyToOne
    private Pais pais;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
