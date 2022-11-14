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
public class Transportador implements Serializable {
    private static final long serialVersionUID = -9095677322316842757L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String nombre;
    private String razonSocial;
    private Double comision;
    
    @OneToOne
    private Ubicacion ubicacion;

    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<Vehiculo> vehiculos;

    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<Chofer> choferes;

    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<Remolque> remolques;

    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<Viaje> viajes;
    
    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<ViajePersonal> viajesPersonales;

    @OneToMany(mappedBy = "transportador")
    @JsonIgnore
    private List<Coincidencia> coincidencias;

    @OneToOne
    private Usuario usuario;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

	public Transportador() {
		this.comision = 5.0;
	}

    
}
