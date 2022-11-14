package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.EstadoViaje;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ViajeModel {

    private String id;
    private String idChofer;
    private String idVehiculo;
    private String idRemolque;
    private String kms;
    private String idUbicacionInicial;
    private String idUbicacionFinal;
    private Integer presupuesto;
    private String idCarga;
    private Date partidaCargaNegativa;
    private Date llegadaCargaNegativa;
    private boolean cargaNegativa;
    private EstadoViaje estadoViaje;
    private String estadoString;
    private List<String> wayPoints;

    // Datos solo necesarios para la api
    private String marcaVehiculo;
    private String descripcionRemolque;
    private String desde;
    private String hasta;
    private String partida;
    private String llegada;
    private String modificacion;

}
