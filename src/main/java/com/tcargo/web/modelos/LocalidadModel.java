package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class LocalidadModel {
    private String id;
    private String nombre;
    private String descripcion;
    private String codigoPostal;

    private String idProvincia;

}