package com.tcargo.web.entidades;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class DadorDeCarga {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String nombre;

    @OneToOne
    private Ubicacion ubicacion;
    private String razonSocial;
    private Double comision;

    @OneToOne
    private Usuario usuario;

    @OneToMany
    private List<Coincidencia> coincidencias;

    @OneToMany
    private List<Viaje> viajes;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

	public DadorDeCarga() {
		this.comision = 5.0;
	}
    
    


}
