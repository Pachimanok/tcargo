package com.tcargo.web.modelos.busqueda;

import com.tcargo.web.enumeraciones.EstadoContraOferta;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BusquedaViajeModel {

    private String tipoDeViaje;
    private List<String> tipoDeCarga;
    private String tipoDeEmbalaje;
    private String tipoDeCamion;
    private String vehiculo;
    private String idModelo;
    private String chofer;
    private String tipoDeRemolque;
    private String producto;
    private String origen;
    private String destino;
    private String fechaDesde;
    private String fechaHasta;
    private String q;
    private Double valor;
    private Double kms;
    private EstadoContraOferta estado;

    public BusquedaViajeModel() {
        tipoDeViaje = "";
        tipoDeEmbalaje = "";
        tipoDeCamion = "";
        vehiculo = "";
        idModelo = "";
        chofer = "";
        tipoDeRemolque = "";
        producto = "";
        origen = "";
        destino = "";
        fechaDesde = "";
        fechaHasta = "";
        q = "";
        tipoDeCarga = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "?tipoDeViaje=" + tipoDeViaje +
                "&tipoDeCarga=" + (tipoDeCarga != null ? tipoDeCarga : "") +
                "&tipoDeEmbalaje=" + tipoDeEmbalaje +
                "&tipoDeCamion=" + tipoDeCamion +
                "&tipoDeRemolque=" + tipoDeRemolque +
                "&origen=" + origen +
                "&destino=" + destino +
                "&fechaDesde=" + producto +
                "&producto=" + fechaDesde +
                "&fechaHasta=" + fechaHasta +
                "&q=" + q +
                "&valor=" + (valor != null ? valor : "") +
                "&kms=" + (kms != null ? kms : "") +
                "&estado=" + (estado != null ? estado : "") +
                "&chofer=" + chofer +
                "&vehiculo=" + vehiculo +
                "&idModelo=" + idModelo;
    }

}
