package com.tcargo.web.modelos;

import java.util.Date;

import com.tcargo.web.enumeraciones.EstadoValoracion;

import lombok.Data;

@Data
public class ValoracionModel {
	
	private String id;
	private String comentarios;
	private Integer valoracion;
	private boolean conformidad;
	private EstadoValoracion estadoValoracion;
	private String idCreador;
	private String nombreCreador;
	private String idReceptor;
	private String nombreReceptor;
	private CoincidenciaModel coincidencia;
	private Date eliminado;
	private Date modificacion;
	
	public ValoracionModel(String comentarios, Integer valoracion, EstadoValoracion estadoValoracion, String idCreador, String idReceptor, CoincidenciaModel coincidencia) {
		
		this.comentarios = comentarios;
		this.valoracion = valoracion;
		this.estadoValoracion = estadoValoracion;
		this.idCreador = idCreador;
		this.idReceptor = idReceptor;
		this.coincidencia = coincidencia;
		
	}
	
	public ValoracionModel() {
		
	}

}
