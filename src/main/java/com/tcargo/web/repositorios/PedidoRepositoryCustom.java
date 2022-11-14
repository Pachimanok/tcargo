package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.modelos.busqueda.BusquedaPedidoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PedidoRepositoryCustom {

    Page<Pedido> buscarPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, Boolean matcheados, boolean excel);

    Page<Pedido> buscarPorCriteriosParaTransportador(Pageable pageable, BusquedaPedidoModel busqueda, List<ContraOferta> contraOfertas, boolean excel);

    Page<Pedido> buscarPorCriteriosParaDador(Pageable pageable, BusquedaPedidoModel busqueda, Usuario usuario, boolean excel);

    Page<Pedido> buscarOfertadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos, boolean excel);

    Page<Pedido> buscarMatcheadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos, boolean excel);

}
