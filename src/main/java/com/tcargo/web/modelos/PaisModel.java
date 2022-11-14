package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class PaisModel {
    private String id;
    private String nombre;
    private String descripcion;
    private String regexPatente;
}