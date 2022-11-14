package com.tcargo.web.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;

import lombok.Data;

@Data
@Entity
public class Invitacion implements Serializable {

	private static final long serialVersionUID = 9119287706196658245L;
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Enumerated(EnumType.STRING)
	private EstadoNotificacion estadoNotificacion;

	@Enumerated(EnumType.STRING)
	private EstadoInvitacion estadoInvitacion;

	@ManyToOne
	private Usuario chofer;

	@ManyToOne
	private Transportador transportador;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modificacion;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date eliminado;

}
