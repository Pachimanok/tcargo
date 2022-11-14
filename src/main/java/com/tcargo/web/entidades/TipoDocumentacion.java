package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Data
@Entity
public class TipoDocumentacion implements Serializable {

    private static final long serialVersionUID = -4291801271584410308L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String nombre;

    private boolean obligatorioVehiculo;
    private boolean obligatorioRemolque;
    private boolean obligatorioChofer;

    @ManyToOne
    private Pais pais;
    private String descripcion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

}
