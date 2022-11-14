package com.tcargo.web.modelos;


import com.tcargo.web.enumeraciones.TipoDeViaje;

import lombok.Data;

@Data
public class TipoRequisitoOfertaModel {
	private String id;
    private String nombre;
    private String descripcion;
    private Boolean obligatorioPedido;
    private Boolean obligatorioOferta;
    private TipoDeViaje tipoDeViaje;
    private String idPais;
    
    public TipoRequisitoOfertaModel () {
    	this.obligatorioPedido = false;
    	this.obligatorioOferta = false;
    }
}