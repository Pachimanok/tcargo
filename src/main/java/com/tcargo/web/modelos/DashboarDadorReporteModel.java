package com.tcargo.web.modelos;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DashboarDadorReporteModel {

    private Integer pedidoPublicados;
    private Integer pedidoConOfertas;
    private Integer matchs;
    private Double totalMatchDolar;
    private Double totalMatchs;
    private List<Map<String, Double>> totales = new ArrayList<>();


    
}
