package com.tcargo.web.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Pedido implements Serializable {
    private static final long serialVersionUID = 1743959293203607986L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private TipoDeViaje tipoDeViaje;
    
    @Enumerated(EnumType.STRING)
    private TipoDeViaje tipoInternacional;

    private EstadoPedido estadoPedido;
    
    @OneToOne
    private Carga carga;
    
    @OneToOne
    private PeriodoDeCarga periodoDeCarga;
    
    @OneToOne
    private PeriodoDeCarga periodoDeDescarga;
    
    @OneToMany
    private List<Requisito> requisitos;


    @ManyToMany
    private List<TipoRequisitoOferta> tipoRequisitos;

    @ManyToOne
    private Ubicacion ubicacionDesde;
    
    @ManyToOne
    private Ubicacion ubicacionHasta;
    
    
    private String kmTotales;

    @Column(name = "is_grupo")
    private boolean grupo;
    private boolean recibirOfertas;
    private boolean pagaAlTransportista;
    @Column(name = "asignado_a_transportador")
    private boolean asignadoATransportador;
    private boolean finalizado;
    private boolean tieneContraOfertas;

    private Double valor;

    @OneToOne
    private Contenedor contenedor;
    
    @ManyToOne
    private Usuario dador;

    @ManyToOne
    private Moneda moneda;

    @OneToMany(mappedBy = "pedido")
    @JsonIgnore
    private List<Coincidencia> coincidencias;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
