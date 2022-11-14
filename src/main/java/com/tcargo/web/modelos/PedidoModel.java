package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import lombok.Data;

import java.util.List;

@Data
public class PedidoModel {

    private Long id;
    private String descripcion;
    private TipoDeViaje tipoDeViaje;
    private TipoDeViaje tipoInternacional;
    private EstadoPedido estadoPedido;
    private String idCarga;
    private String idPeriodoDeCarga;
    private String idPeriodoDeDescarga;
    private long difereciaHorasCarga;
    private long difereciaHorasDescarga;
    private List<String> idRequisitos;
    private List<String> nombresRequisitos;
    private String idUbicacionDesde;
    private String idUbicacionHasta;
    private Boolean isGrupo;
    private String kmTotales;
    private Boolean recibirOfertas;
    private String idMoneda;
    private Double valor;
    private Boolean pagaAlTransportista;
    private Boolean asignadoATransportador;
    private boolean finalizado;
    private String idContenedor;
    private String idDador;
    private List<String> idCoincidencias;

}
