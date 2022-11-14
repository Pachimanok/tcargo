package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoRemolque;
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
public interface TipoRemolqueRepository extends JpaRepository<TipoRemolque, String> {

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL")
    Page<TipoRemolque> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL")
    List<TipoRemolque> buscarActivos(Sort sort);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL ORDER BY a.caracteristicas")
    List<TipoRemolque> buscarActivos();

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NOT NULL")
    Page<TipoRemolque> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoRemolque a")
    Page<TipoRemolque> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL AND (a.caracteristicas LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q)")
    Page<TipoRemolque> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas")
    List<TipoRemolque> buscarActivos(Sort sort, @Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL AND a.caracteristicas = :caracteristicas")
    List<TipoRemolque> buscarTipoRemolquePorCaracteristicas(@Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoRemolque a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoRemolque> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoRemolque a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoRemolque> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    Page<TipoRemolque> buscarActivosPorPais(Pageable pageable, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoRemolque a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    List<TipoRemolque> buscarActivosPorPais(Sort sort, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT tr FROM TipoRemolque tr WHERE tr.eliminado IS NULL AND tr.pais.id LIKE :idPais")
    List<TipoRemolque> activosPorPais(@Param(ID_PAIS_LABEL) String idPais);

}

