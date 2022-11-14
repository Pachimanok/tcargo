package com.tcargo.web.modelos.busqueda;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class BusquedaVehiculoModel {

    private String marca;
    private String modelo;
    private String tipoVehiculo;
    private String anio;
    private String query;
    private boolean tieneSensores;
    private boolean tieneRastreo;

    private List<String> idsTipoCarga;

    public BusquedaVehiculoModel() {
        marca = "";
        modelo = "";
        tipoVehiculo = "";
        anio = "";
        query = "";
        idsTipoCarga = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "?marca=" + marca +
                "&modelo=" + modelo +
                "&tipoVehiculo=" + tipoVehiculo +
                "&anio=" + anio +
                "&query=" + query +
                "&sensores=" + tieneSensores +
                "&rastreo=" + tieneRastreo +
                "&tipoCarga=" + StringUtils.collectionToCommaDelimitedString(idsTipoCarga);
    }
}
