package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.TipoDeViaje;
import com.tcargo.web.enumeraciones.TipoPeso;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRestModel {

    // ATRIBUTOS DEL CONTENEDOR
    private String idContenedor;
    private String descripcionContenedor;
    private String direccionRetiroContenedor;
    private Double latitudRetiroContenedor;
    private Double longitudRetiroContenedor;
    private String direccionEntregaContenedor;
    private Double latitudEntregaContenedor;
    private Double longitudEntregaContenedor;
    private String idTipoContenedor;

    // ATRIBUTOS DE LA CARGA
    private String idCarga;
    private String descripcionCarga;
    private boolean cargaCompleta;
    private int peso;
    private TipoPeso tipoPeso;
    private Double metrosCubicos;
    private int cantidadCamiones;
    private boolean conCustodia;
    private boolean seguroCarga;
    private boolean indivisible;
    private String idProducto;
    private List<String> idsTipoCarga;
    private String idTipoEmbalaje;
    private String idTipoVehiculo;
    private String idTipoRemolque;

    // ATRIBUTOS UBICACION DESDE
    private String idDireccionDesde;
    private String direccionDesde;
    private Double latitudDesde;
    private Double longitudDesde;

    // ATRIBUTOS UBICACION HASTA
    private String idDireccionHasta;
    private String direccionHasta;
    private Double latitudHasta;
    private Double longitudHasta;

    // ATRIBUTOS PERIODO DE CARGA
    private String idPeriodoCarga;
    private String inicioPeriodoCarga;
    private String finPeriodoCarga;
    private boolean cargaNocturnaPeriodoCarga;
    private String descripcionPeriodoCarga;

    // ATRIBUTOS PERIODO DE DESCARGA
    private String idPeriodoDescarga;
    private String inicioPeriodoDescarga;
    private String finPeriodoDescarga;
    private boolean cargaNocturnaPeriodoDescarga;
    private String descripcionPeriodoDescarga;

    // ATRIBUTOS PEDIDO
    private Long idPedido;
    private String idDador;
    private List<String> idsRequisitos;
    private boolean recibirOfertas;
    private String idMoneda;
    private Double valor;
    private String condicionPago;
    private boolean pagaAlTransportista;
    private boolean asignadoATransportador;

    // SI EL PEDIDO LO CREA DESDE EL LISTADO DE VIAJES, VIENE ESTE PARAMETRO
    private String mailTransportador;
    private TipoDeViaje tipoDeViaje;
    private TipoDeViaje tipoInternacional;

}
