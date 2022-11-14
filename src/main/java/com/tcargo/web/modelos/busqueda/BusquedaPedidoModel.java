package com.tcargo.web.modelos.busqueda;

import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import lombok.Data;

@Data
public class BusquedaPedidoModel {

    private Boolean recibirOfertas;
    private EstadoPedido estadoPedido;
    private TipoDeViaje tipoDeViaje;
    private Double valor;
    private String fechaCarga;
    private String fechaDescarga;
    private String idTipoCarga;
    private String idProducto;
    private String otro;

    public BusquedaPedidoModel() {
        fechaCarga = "";
        fechaDescarga = "";
        idTipoCarga = "";
        idProducto = "";
        otro = "";
    }

    @Override
    public String toString() {
        return "?tipoDeViaje=" + (tipoDeViaje != null ? tipoDeViaje : "") +
                "&idTipoCarga=" + idTipoCarga +
                "&idProducto=" + idProducto +
                (recibirOfertas != null ? ("&recibirOfertas=" + recibirOfertas) : "") +
                "&otro=" + otro +
                "&fechaCarga=" + fechaCarga +
                "&fechaDescarga=" + fechaDescarga +
                "&estadoPedido=" + (estadoPedido != null ? estadoPedido : "") +
                "&valor=" + (valor != null ? valor : "");
    }
}
