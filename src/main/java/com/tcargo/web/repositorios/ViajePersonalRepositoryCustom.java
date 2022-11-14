package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.ViajePersonal;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ViajePersonalRepositoryCustom {

    Page<ViajePersonal> buscarPorCriterios(BusquedaViajeModel viajeM, Transportador transportador, Pageable pageable, boolean isTransportador);

}
