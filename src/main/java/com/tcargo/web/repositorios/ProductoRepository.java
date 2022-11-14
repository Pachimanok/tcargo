package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL")
    Page<Producto> buscarActivos(Pageable pageable);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL")
    List<Producto> buscarActivos(Sort sort);

    @Query("SELECT a from Producto a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<Producto> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<Producto> buscarActivos();

    @Query("SELECT a from Producto a WHERE a.eliminado IS NOT NULL")
    Page<Producto> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Producto a")
    Page<Producto> buscarTodos(Pageable pageable);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL AND (a.nombre LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q)")
    Page<Producto> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    List<Producto> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    Page<Producto> buscarActivosPorPais(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    List<Producto> buscarActivosPorPais(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Producto a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<Producto> buscarProductoPorNombre(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.eliminado IS NULL AND p.pais.id LIKE :idPais")
    List<Producto> activosPorPais(@Param(ID_PAIS_LABEL) String idPais);

}

