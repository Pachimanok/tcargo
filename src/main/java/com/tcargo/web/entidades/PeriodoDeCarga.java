package com.tcargo.web.entidades;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class PeriodoDeCarga implements Serializable {

    private static final long serialVersionUID = 2365992523898453280L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String inicio;
    private String finalizacion;
    private String horaInicio;
    private String horaFinalizacion;
    private Boolean cargaNocturna;
    private String descripcion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
