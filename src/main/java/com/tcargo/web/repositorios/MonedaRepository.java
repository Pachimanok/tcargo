package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Moneda;
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
public interface MonedaRepository extends JpaRepository<Moneda, String> {

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<Moneda> buscarActivos();

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL")
    Page<Moneda> buscarActivos(Pageable pageable);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL")
    List<Moneda> buscarActivos(Sort sort);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre)")
    Page<Moneda> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre)")
    List<Moneda> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NOT NULL")
    Page<Moneda> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Moneda a")
    Page<Moneda> buscarTodos(Pageable pageable);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<Moneda> buscarMonedaPorNombre(@Param("nombre") String nombre);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    Page<Moneda> buscarActivosPorPais(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Moneda a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    List<Moneda> buscarActivosPorPais(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Moneda a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<Moneda> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from Moneda a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<Moneda> buscarActivosPorPais(Sort sort, String idPais);
    
    @Query("SELECT a from Moneda a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<Moneda> buscarActivosPorPais(String idPais);
}

