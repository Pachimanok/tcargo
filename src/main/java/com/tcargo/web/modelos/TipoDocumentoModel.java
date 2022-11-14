package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class TipoDocumentoModel {
    private String id;
    private String nombre;
    private String descripcion;

    private String idPais;

}