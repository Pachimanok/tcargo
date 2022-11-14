package com.tcargo.web.modelos;

import lombok.Data;

import java.util.List;

@Data
public class DocumentacionApiModel {

    private String vencimiento;
    private String idTipoDocumentacion;
    private List<Base64MultipartFile> archivos;
    private ChoferModel chofer;
    private RemolqueModel remolque;
    private VehiculoModel vehiculo;

}
