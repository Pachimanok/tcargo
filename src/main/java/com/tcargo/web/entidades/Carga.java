package com.tcargo.web.entidades;

import com.tcargo.web.enumeraciones.TipoPeso;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Carga implements Serializable {
    private static final long serialVersionUID = 2140785912567980419L;
   
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String descripcion;
    
    private TipoPeso tipoPeso;
    private Double metrosCubicos;
    
    private Integer peso;
    private Integer cantidadCamiones;
    
    private boolean conCustodia;
    private boolean seguroCarga;
    private boolean indivisible;

    private boolean cargaCompleta;
    
    @ManyToOne
    private TipoVehiculo tipoVehiculo;
    
    @ManyToOne
    private TipoRemolque tipoRemolque;
    
    @ManyToMany
    private List<TipoCarga> tipoCargas;
    
    @ManyToOne
    private TipoEmbalaje tipoEmbalaje;
    
    @ManyToOne
    private Producto producto;


    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
