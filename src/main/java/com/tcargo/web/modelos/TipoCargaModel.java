package com.tcargo.web.modelos;

import lombok.Data;

@Data
public class TipoCargaModel {

    private String id;
    private String caracteristicas;
    private String descripcion;
    private boolean isPalet;
    private String idPais;

}