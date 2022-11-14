package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Contenedor implements Serializable {
    private static final long serialVersionUID = -8891913900103657662L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String descripcion;
    
    @OneToOne
    private TipoContenedor tipoContenedor;
    
    @OneToOne(cascade = {CascadeType.ALL})
    private Ubicacion ubicacionRetiro;
    
    @OneToOne(cascade = {CascadeType.ALL})
    private Ubicacion ubicacionEntrega;
    
    @OneToMany
    private List<Documentacion> documentacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
