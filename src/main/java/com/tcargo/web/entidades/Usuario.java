package com.tcargo.web.entidades;

import com.tcargo.web.enumeraciones.Rol;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
public class Usuario implements Serializable {

    private static final long serialVersionUID = -5408954174563706625L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(unique = true)
    private String mail;
    private String clave;
    private String nombre;
    private String telefono;
    private String cuit;
    private boolean cuitVerificado;
    private String responsable;
    private boolean verificado;
    private boolean activo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimoRecuperarClave;

    @ManyToOne
    private Pais pais;

    @OneToOne
    private Ubicacion ubicacion;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

}
