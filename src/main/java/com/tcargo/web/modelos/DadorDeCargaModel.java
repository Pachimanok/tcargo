package com.tcargo.web.modelos;

import lombok.Data;

import java.util.List;

@Data
public class DadorDeCargaModel {
    private String id;
    private String nombre;
    private String idUbicacion;
    private List<String> idCoincidencias;
    private String razonSocial;
    private String idUsuario;

}