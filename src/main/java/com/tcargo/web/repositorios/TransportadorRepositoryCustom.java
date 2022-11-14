package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.modelos.busqueda.BusquedaTransportadorModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransportadorRepositoryCustom {

    Page<Transportador> buscarFiltrados(Pageable pageable, BusquedaTransportadorModel busqueda, boolean excel);

}
