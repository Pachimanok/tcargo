package com.tcargo.web.modelos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VehiculoModel {
    private String id;
    private String idTipoVehiculo;
    private String idTipoRemolque;
    private String idModelo;
    private String dominio;
    private boolean rastreoSatelital;
    private boolean sensores;
    private String anioFabricacion;
    private String idTransportador;
    private List<String> idCargas = new ArrayList<>();

}