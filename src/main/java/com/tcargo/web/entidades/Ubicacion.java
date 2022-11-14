package com.tcargo.web.entidades;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.Spatial;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Indexed
@Spatial
public class Ubicacion implements Serializable {
    private static final long serialVersionUID = -6044435567656820601L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @ToString.Include
    @EqualsAndHashCode.Include
    private String id;
   
    @ToString.Include(name = "dir")
    private String direccion;
    

    @ToString.Include
    private String descripcion;
   
    @Latitude
    @ToString.Include
    private Double latitud;
    
    @Longitude
    @ToString.Include
    private Double longitud;
    
    private Boolean isMasterPoint;
    
    @ManyToOne
    private Pais pais;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eliminado;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificacion;

}
