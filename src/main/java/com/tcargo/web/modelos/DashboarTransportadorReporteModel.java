package com.tcargo.web.modelos;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DashboarTransportadorReporteModel {

    private Integer cargasNegativas;
    private Integer vehiculosTotales;
    private Integer choferesTotales;
    private Integer cargasOfertas;
    private Integer matchs;
    private Integer viajes;
    private Double totalMatchs;
    private String simboloLocal;
    private Double totalMatchDolar;
    private List<Map<String, Double>> totales = new ArrayList<>();

    
}
