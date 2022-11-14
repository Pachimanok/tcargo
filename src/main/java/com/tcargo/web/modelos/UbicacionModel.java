package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class UbicacionModel {

    private String id;
    private String idProvisiorio;
    private String direccion;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String codigoPostal;
    private Boolean isMasterPoint;
    private String idPais;

}