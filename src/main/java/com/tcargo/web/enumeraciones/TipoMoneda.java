package com.tcargo.web.enumeraciones;

public enum TipoMoneda {

    PESOS_ARG("Pesos Argentinos", "$"), DOLAR("Dolares", "U$$"), PESO_CHILENO("Pesos Chilenos", "$");

    
    private TipoMoneda(String texto, String codigo) {
    	this.texto = texto;
    	this.codigo = codigo;
    }
    
    private String texto;
	private String codigo;
	
	public String getCodigo() {
		return codigo;
	}
	
	public String getTexto() {
		return texto;
	}
}
