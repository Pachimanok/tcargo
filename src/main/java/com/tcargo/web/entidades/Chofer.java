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
public class Chofer implements Serializable {

    private static final long serialVersionUID = -8645116754945685023L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String emailAdicional;
    private String telefonoAdicional;
    private String documento;
    
    @OneToOne
    private Usuario usuario;
    
    @ManyToOne
    private TipoDocumento tipoDocumento;

    @ManyToOne
    private Transportador transportador;
    
    @OneToMany(mappedBy = "chofer")
    @JsonIgnore
    private List<Documentacion> documentacion;
    
    @OneToMany(mappedBy = "chofer")
    @JsonIgnore
    private List<Viaje> viajes;
    
    @OneToMany(mappedBy = "chofer")
    @JsonIgnore
    private List<ViajePersonal> viajesPersonales;

    @OneToMany
    private List<TipoCarga> tipoCarga;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
