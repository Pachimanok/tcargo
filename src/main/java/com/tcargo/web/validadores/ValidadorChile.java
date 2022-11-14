package com.tcargo.web.validadores;

import com.tcargo.web.entidades.Usuario;

public class ValidadorChile implements ValidadorUsuario{

	@Override
	public boolean validar(Usuario usuario) {
		return true;
	}

}
