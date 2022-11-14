package com.tcargo.web.modelos.busqueda;

import lombok.Data;

@Data
public class BusquedaTransportadorModel {

    private String email;
    private String domicilio;
    private String nombre;
    private String telefono;
    private String idPais;

    public BusquedaTransportadorModel() {
        email = "";
        domicilio = "";
        nombre = "";
        telefono = "";
    }

    @Override
    public String toString() {
        return "?email=" + email +
                "&domicilio=" + domicilio +
                "&nombre=" + nombre +
                "&telefono=" + telefono;
    }

}
