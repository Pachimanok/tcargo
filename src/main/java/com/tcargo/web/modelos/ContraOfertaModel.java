package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.EstadoContraOferta;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ContraOfertaModel {

    private String id;
    private Boolean seguroCarga;
    private Boolean incluyeBajadaAPiso;
    private Boolean aduana;
    private Boolean carteleriaCargaPeligrosa;
    private Boolean incluyeIva;
    private Double valor;
    private Integer diasLibres;
    private String idDador;
    private String idTransportador;
    private String idCreador;
    private String idVehiculo;
    private String idRemolque;
    private String idPedido;
    private Long idPedidoLong;
    private String idChofer;
    private String idMoneda;
    private String simboloMoneda;
    private Date eliminado;
    private Date modificacion;
    private String comentarios;
    private Boolean isFinal;
    private List<String> idRequisitosContraOferta = new ArrayList<>();
    private List<String> requisitosNombres = new ArrayList<>();
    private EstadoContraOferta estado;

}