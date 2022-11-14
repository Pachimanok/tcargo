package com.tcargo.web.modelos;

import com.tcargo.web.entidades.TipoDocumento;
import lombok.Data;

import java.util.List;

@Data
public class ChoferModel {

    private String id;
    private TipoDocumento tipoDocumento;
    private String idTipoDocumento;
    private String documento;
    private String emailAdicional;
    private String telefonoAdicional;
    private String idTransportador;
    private UsuarioModel usuario = new UsuarioModel();
    private List<String> idDocumentacion;
    private List<String> idTipoCarga;

}