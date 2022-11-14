package com.tcargo.web.modelos;

import java.util.List;

import com.tcargo.web.enumeraciones.TipoPeso;
import lombok.Data;

@Data
public class CargaModel {

    private String id;
    private String descripcion;
    private Boolean cargaCompleta;
    private Integer peso;
    private TipoPeso tipoPeso;
    private Double metrosCubicos;
    private Integer cantidadCamiones;
    private Boolean conCustodia;
    private Boolean seguroCarga;
    private Boolean indivisible;
    private String idProducto;
    private List<String> idTipoCargas;
    private String idTipoEmbalaje;
    private String idTipoRemolque;
    private String idTipoVehiculo;

}
