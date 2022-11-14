package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.modelos.busqueda.BusquedaVehiculoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculoRepositoryCustom {

    Page<Vehiculo> buscarPorCriterios(Pageable pageable, BusquedaVehiculoModel busqueda, String idTransportador);

}
