package com.tcargo.web.modelos;

import java.util.Date;
import java.util.List;


import com.tcargo.web.enumeraciones.EstadoViaje;

import lombok.Data;

@Data
public class ViajePersonalModel {

    private String id;
	private String detalle;
    private String idChofer;
    private String idVehiculo;
    private String idRemolque;
    private String kms;
    private String fechaInicio;
    private String fechaFinal;
    private EstadoViaje estadoViaje;
    private List<CoincidenciaParaViajePropioModel> coincidencias;    
    private String idTransportador;
    private List<String> nuevoOrdenIds;
    private Date modificacion;
    
//    PARA RENDERIZAR
    
    private String chofer;
    private String vehiculo;
    private String dominioV;
    private String remolque;
    private String dominioR;
    private List<UbicacionModel> ubicaciones;
}
