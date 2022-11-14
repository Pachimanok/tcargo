package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.Rol;
import lombok.Data;

@Data
public class UsuarioModelPerfil {

    private String id;
    private String nombre;
    private String usuario;
    private String clave;
    private String clave1;
    private String clave2;
    private String telefono;
    private String cuit;
    private boolean cuitVerificado;
    private String mail;
    private String descripcion;
    private String idPais;
    private String idUbicacion;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String nombreDador;
    private String razonSocialDador;
    private String nombreTransportador;
    private String razonSocialTransportador;
    private Rol rol;
    
    //CHOFER
    private String idTipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String emailAdicional;
    private String telefonoAdicional;
    private String idTransportador; 

}
