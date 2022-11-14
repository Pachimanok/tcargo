package com.tcargo.web.modelos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CoincidenciaParaViajePropioModel {

    private String id;
    private String idTransportador;
    private List<String> tipoCarga = new ArrayList<>();
    private String producto;
    private String dador;
    private Long idPedido;
    private UbicacionModel desde;
    private UbicacionModel hasta;
    private String inicioCarga;
    private String finalCarga;
    private String inicioDescarga;
    private String finalDescarga;
    private Boolean asignadoAViajePersonal;


}
