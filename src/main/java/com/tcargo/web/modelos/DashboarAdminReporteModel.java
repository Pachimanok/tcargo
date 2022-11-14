package com.tcargo.web.modelos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DashboarAdminReporteModel {

    private Integer cargasTotales;
    private Integer cargasConOfertas;
    private Integer cargasConFlete;
    private Integer cargasMatcheados;
    private Integer cargasNegativas;
    private Integer transportadorasTotales;
    private Integer vehiculosTotales;
    private List<Integer> cantidadCamionerPorTransportador = new ArrayList<>();
    private List<String> nombres = new ArrayList<>();
    private List<Integer> cantidadCamionesPorTipo = new ArrayList<>();
    private List<String> tipoCamiones = new ArrayList<>();

}
