package com.tcargo.web.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Entity
@Data
public class Token implements Serializable{
	
	private static final long serialVersionUID = -970408885132373782L;

	@Id
    @GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	private String texto;
	
	@ManyToOne
	private Usuario usuario;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creado;
	
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
}

