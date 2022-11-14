package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Viaje;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ViajeRepositoryCustom {

    Page<Viaje> buscarPorCriterios(BusquedaViajeModel viajeM, Transportador transportador, Pageable pageable, boolean isTransportador);

}
