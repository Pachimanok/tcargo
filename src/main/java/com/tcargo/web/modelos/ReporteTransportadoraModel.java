package com.tcargo.web.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTransportadoraModel {

    private String id;
    private String nombre;
    private Long choferes;
    private Long ofertas;
    private Long remolques;
    private Long vehiculos;
    private Long coincidencias;
    private String direccion;
    private String telefono;
    private String mail;
    private Date modificacion;

}

