package com.tcargo.web.entidades;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Documentacion implements Serializable {

    private static final long serialVersionUID = -185466543879822589L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String titulo;

    @ElementCollection(fetch = FetchType.EAGER) // 1
    @CollectionTable(name = "archivos", joinColumns = @JoinColumn(name = "id")) // 2
    @Column(name = "path")
    private List<String> archivos = new ArrayList<>();

    @ManyToOne
    private TipoDocumentacion tipoDocumentacion;
    @ManyToOne
    private Chofer chofer;
    @ManyToOne
    private Vehiculo vehiculo;
    @ManyToOne
    private Remolque remolque;

    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimiento;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;

}
