package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoEmbalaje;
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
public interface TipoEmbalajeRepository extends JpaRepository<TipoEmbalaje, String> {

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL")
    Page<TipoEmbalaje> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL")
    List<TipoEmbalaje> buscarActivos(Sort sort);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL ORDER BY a.caracteristicas")
    List<TipoEmbalaje> buscarActivos();

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NOT NULL")
    Page<TipoEmbalaje> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoEmbalaje a")
    Page<TipoEmbalaje> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL AND (a.caracteristicas LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q)")
    Page<TipoEmbalaje> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas")
    List<TipoEmbalaje> buscarActivos(Sort sort, @Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL AND a.caracteristicas = :caracteristicas")
    List<TipoEmbalaje> buscarTipoEmbalajePorCaracteristicas(@Param("caracteristicas") String caracteristicas);

    @Query("SELECT a from TipoEmbalaje a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoEmbalaje> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoEmbalaje a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoEmbalaje> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    Page<TipoEmbalaje> buscarActivosPorPais(Pageable pageable, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoEmbalaje a WHERE a.eliminado IS NULL AND a.caracteristicas LIKE :caracteristicas AND a.pais.id LIKE :idPais")
    List<TipoEmbalaje> buscarActivosPorPais(Sort sort, @Param("caracteristicas") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT te FROM TipoEmbalaje te WHERE te.eliminado IS NULL AND te.pais.id LIKE :idPais")
    List<TipoEmbalaje> activosPorPais(@Param(ID_PAIS_LABEL) String idPais);

}

