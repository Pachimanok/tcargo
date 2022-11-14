package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoContenedor;
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
public interface TipoContenedorRepository extends JpaRepository<TipoContenedor, String> {

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL")
    Page<TipoContenedor> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL")
    List<TipoContenedor> buscarActivos(Sort sort);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL ORDER BY a.caracteristicas")
    List<TipoContenedor> buscarActivos();

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NOT NULL")
    Page<TipoContenedor> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoContenedor a")
    Page<TipoContenedor> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL AND ( a.caracteristicas LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q )")
    Page<TipoContenedor> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas")
    List<TipoContenedor> buscarActivos(Sort sort, @Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL AND a.caracteristicas = :caracteristicas")
    List<TipoContenedor> buscarTipoContenedorPorCaracteristicas(@Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoContenedor a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoContenedor> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoContenedor a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoContenedor> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL AND ( a.caracteristicas LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q ) AND a.pais.id LIKE :idPais")
    Page<TipoContenedor> buscarActivosPorPais(Pageable pageable, @Param("q") String q, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoContenedor a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    List<TipoContenedor> buscarActivosPorPais(Sort sort, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);
}

