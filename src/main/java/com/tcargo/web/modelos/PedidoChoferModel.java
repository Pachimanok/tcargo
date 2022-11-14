package com.tcargo.web.modelos;

import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.enumeraciones.EstadoViaje;
import lombok.Data;

@Data
public class PedidoChoferModel {

    private EstadoViaje estadoViaje;
    private Pedido pedido;

}
