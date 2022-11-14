package com.tcargo.web.modelos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RemolqueModel {

    private String id;
    private String idTipoRemolque;
    private String dominio;
    private String anioFabricacion;
    private String idTransportador;
    private List<String> idCargas = new ArrayList<>();

}