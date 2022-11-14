package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.Rol;
import lombok.Data;

@Data
public class UsuarioModel {

    private String id;
    private String nombre;
    private String clave;
    private String clave1;
    private String clave2;
    private Double comisionDador;
    private Double comisionTransportador;
    private String telefono;
    private String cuit;
    private String mail;
    private String descripcion;
    private String idPais;
    private String idUbicacion;
    private Rol rol;
    private boolean verificado;
    private boolean activo;

}
