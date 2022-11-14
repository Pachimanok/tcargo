package com.tcargo.web.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Remolque implements Serializable {

    private static final long serialVersionUID = -8891913900103657662L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String dominio;
    private String anioFabricacion;

    @ManyToOne
    private TipoRemolque tipoRemolque;
    @OneToMany(mappedBy = "remolque")
    @JsonIgnore
    private List<Documentacion> documentacion;
    @ManyToMany
    private List<TipoCarga> tipoCargas;
    @ManyToOne
    private Transportador transportador;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
