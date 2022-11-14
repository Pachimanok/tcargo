package com.tcargo.web.validadores;

import java.util.HashMap;
import java.util.Map;

public class ValidadorFactory {
	private static ValidadorFactory instance;
	
	private Map<String, ValidadorUsuario> validadores; 
	
	private ValidadorFactory() {
		validadores = new HashMap<>();
		validadores.put("ar", new ValidadorArgentina());
		validadores.put("cl", new ValidadorChile());
	}
	
	public static ValidadorFactory getInstance() {
		if(instance == null) {
			instance = new ValidadorFactory();
		}
		return instance;
	}
	
	public ValidadorUsuario get(String codigo) {
		if(codigo != null) {
			ValidadorUsuario validador = validadores.get(codigo);
			if(validador != null) {
				return validador;
			}
		}
		
		return new ValidadorNull();
	}
}
