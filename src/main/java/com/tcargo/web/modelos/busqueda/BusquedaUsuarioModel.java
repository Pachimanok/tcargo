package com.tcargo.web.modelos.busqueda;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BusquedaUsuarioModel {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFinalizacion;
    private String otro;
    private String rol;
    private String idPais;
    private String idUsuario;

    @Override
    public String toString() {
        return "?fechaInicio=" + (fechaInicio != null ? fechaInicio.toString() : "")
                + "&fechaFinalizacion=" + (fechaFinalizacion != null ? fechaFinalizacion.toString() : "")
                + "&otro=" + (otro != null ? otro : "")
                + "&rol=" + (rol != null ? rol : "")
                + "&idPais=" + (idPais != null ? idPais : "");
    }

}
