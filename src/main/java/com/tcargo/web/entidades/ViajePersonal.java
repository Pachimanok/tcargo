package com.tcargo.web.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tcargo.web.enumeraciones.EstadoViaje;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class ViajePersonal implements Serializable {
	private static final long serialVersionUID = 5280568346761005560L;
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;
	private String detalle;
	private String kms;
	private boolean internacional;

	@ManyToOne
	private Chofer chofer;
	@ManyToOne
	private Vehiculo vehiculo;
	@ManyToOne
	private Remolque remolque;
	
	@ElementCollection(targetClass = String.class)
	private List<String> nuevoOrdenIds;
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaInicio;

	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaFinal;
	
	@Enumerated(EnumType.STRING)
	private EstadoViaje estadoViaje;
	@ManyToOne
	private Transportador transportador;
	@JsonIgnore
	@OneToMany
	private List<Coincidencia> coincidencias;
	@Temporal(TemporalType.TIMESTAMP)
	private Date modificacion;
	@Temporal(TemporalType.TIMESTAMP)
	private Date eliminado;

}
