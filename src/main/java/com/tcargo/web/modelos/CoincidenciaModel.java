package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class CoincidenciaModel {

    private String id;
    private Double costo;
    private String detalle;
    private String conformidadDador;
    private Boolean finalizado;
    private Boolean aprobado;
    private String idComision;
    private String idViaje;
    private ViajeModel viaje;
    private String idTransportador;
    private Long idPedido;

}
