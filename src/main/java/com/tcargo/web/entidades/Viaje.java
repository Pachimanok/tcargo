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
public class Viaje implements Serializable {
	private static final long serialVersionUID = 5280568346761005560L;
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@ManyToOne
	private Chofer chofer;
	@ManyToOne
	private Vehiculo vehiculo;
	@ManyToOne
	private Remolque remolque;
	private boolean cargaNegativa;

	@Temporal(TemporalType.TIMESTAMP)
	private Date partidaCargaNegativa;

	@Temporal(TemporalType.TIMESTAMP)
	private Date llegadaCargaNegativa;

	private String kms;

	@Enumerated(EnumType.STRING)
	private EstadoViaje estadoViaje;
	@ElementCollection(targetClass = String.class)
	private List<String> wayPoints;
	@ManyToOne
	private Ubicacion ubicacionInicial;
	@ManyToOne
	private Transportador transportador;
	@ManyToOne
	private Ubicacion ubicacionFinal;
	private Integer presupuesto;

	@OneToOne
	private Carga carga;

	@JsonIgnore
	@OneToMany(mappedBy = "viaje")
	private List<Coincidencia> coincidencias;
	@Temporal(TemporalType.TIMESTAMP)
	private Date modificacion;
	@Temporal(TemporalType.TIMESTAMP)
	private Date eliminado;
	private boolean internacional;

}
