package com.tcargo.web.modelos;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PedidoCercanoModel {

    private Long id;
    @EqualsAndHashCode.Include
    private String idCarga;
    private String producto;
    private String fechaCargaDesde;
    private String fechaCargaHasta;
    private String fechaDescargaDesde;
    private String fechaDescargaHasta;
    private String desde;
    private String hasta;
    private Double latDesde;
    private Double lngDesde;

}
