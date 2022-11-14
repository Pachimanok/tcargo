package com.tcargo.web.modelos;

import com.tcargo.web.enumeraciones.Rol;
import lombok.Data;

import java.util.Date;

@Data
public class UsuarioModelReporte {

    private String id;
    private String nombre;
    private String usuario;
    private String telefono;
    private String cuit;
    private String mail;
    private String idPais;
    private String nombrePais;
    private String razonSocialDador;
    private String razonSocialTransportador;
    private Date modificacion;
    private Integer pedidos;
    private Rol rol;

}
