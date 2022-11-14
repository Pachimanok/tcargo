package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.modelos.busqueda.BusquedaUsuarioModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioRepositoryCustom {

    Page<Usuario> buscarPorCriterios(Pageable pageable, BusquedaUsuarioModel busqueda, boolean excel);

}
