package com.tcargo.web.modelos;

import lombok.Data;

import java.util.List;

@Data
public class CargaNegativaModel {

    private String id;
    private String idChofer;
    private String idVehiculo;
    private String idRemolque;
    private String kms;
    private String partidaCargaNegativa;
    private String llegadaCargaNegativa;
    private UbicacionModel ubicacionInicial;
    private UbicacionModel ubicacionFinal;
    private String idTransportador;
    private Integer presupuesto;
    private String idCarga;
    private boolean cargaNegativa;
    private List<String> wayPoints;

}
