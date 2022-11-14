package com.tcargo.web.modelos;

import lombok.Data;

import java.util.List;

@Data
public class TransportadorModel {
    private String id;
    private String nombre;
    private String idUbicacion;
    private List<String> idTipoVehiculo;
    private List<String> idModelo;
    private List<String> idChoferes;
    private List<String> idRemolques;
    private List<String> idviajes;
    private List<String> idCoincidencias;
    private String razonSocial;
    private String idUsuario;

}