package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class MonedaModel {
    private String id;
    private String nombre;
    private String simbolo;
    private String descripcion;
    private String idPais;
}