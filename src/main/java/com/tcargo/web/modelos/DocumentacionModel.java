package com.tcargo.web.modelos;

import lombok.Data;

import java.util.List;

@Data
public class DocumentacionModel {

    private String id;
    private String vencimiento;
    private String titulo;
    private String idTipoDocumentacion;
    private String idChofer;
    private String idRemolque;
    private String idVehiculo;
    private List<String> nombresArchivos;

}