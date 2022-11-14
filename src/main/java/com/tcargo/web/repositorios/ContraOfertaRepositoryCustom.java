package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.modelos.busqueda.BusquedaHistorialModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContraOfertaRepositoryCustom {

    Page<ContraOferta> buscarPorTransportador(Pageable pageable, String idTransportador, BusquedaHistorialModel busqueda);

    Page<ContraOferta> buscarPorDador(Pageable pageable, String idDador, BusquedaHistorialModel busqueda);

    Page<ContraOferta> buscarPorCriterios(Pageable pageable, Long idPedido, Double valor, String vehiculo, String otro, boolean excel);

}
