package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class TipoDocumentacionModel {
    private String id;
    private String nombre;
    private String descripcion;
    private String idPais;
    private Boolean obligatorioVehiculo;
    private Boolean obligatorioRemolque;
    private Boolean obligatorioChofer;
}