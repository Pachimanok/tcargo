package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoCarga;
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
public interface TipoCargaRepository extends JpaRepository<TipoCarga, String> {

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL")
    Page<TipoCarga> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL")
    List<TipoCarga> buscarActivos(Sort sort);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL ORDER BY a.caracteristicas")
    List<TipoCarga> buscarActivos();

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NOT NULL")
    Page<TipoCarga> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoCarga a")
    Page<TipoCarga> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL AND (a.caracteristicas LIKE :q OR a.pais.nombre LIKE :q OR a.descripcion LIKE :q)")
    Page<TipoCarga> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas")
    List<TipoCarga> buscarActivos(Sort sort, @Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL AND a.caracteristicas = :caracteristicas")
    List<TipoCarga> buscarTipoCargaPorCaracteristicas(@Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoCarga a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoCarga> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoCarga a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoCarga> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    Page<TipoCarga> buscarActivosPorPais(Pageable pageable, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoCarga a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    List<TipoCarga> buscarActivosPorPais(Sort sort, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT tc FROM TipoCarga tc WHERE tc.eliminado IS NULL AND tc.pais.id LIKE :idPais")
    List<TipoCarga> activosPorPais(@Param(ID_PAIS_LABEL) String idPais);

}

