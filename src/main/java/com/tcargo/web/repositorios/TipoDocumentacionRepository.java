package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoDocumentacion;
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
public interface TipoDocumentacionRepository extends JpaRepository<TipoDocumentacion, String> {

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL")
    Page<TipoDocumentacion> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL")
    List<TipoDocumentacion> buscarActivos(Sort sort);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<TipoDocumentacion> buscarActivos();

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NOT NULL")
    Page<TipoDocumentacion> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoDocumentacion a")
    Page<TipoDocumentacion> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND (a.nombre LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q)")
    Page<TipoDocumentacion> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    List<TipoDocumentacion> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<TipoDocumentacion> buscarTipoDocumentacionPorNombre(@Param("nombre") String nombre);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.obligatorioVehiculo = true AND a.pais.id LIKE :idPais")
    List<TipoDocumentacion> buscarObligatorioVehiculo(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.obligatorioRemolque = true AND a.pais.id LIKE :idPais")
    List<TipoDocumentacion> buscarObligatorioRemolque(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.obligatorioChofer = true AND a.pais.id LIKE :idPais")
    List<TipoDocumentacion> buscarObligatorioChofer(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoDocumentacion> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoDocumentacion> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    Page<TipoDocumentacion> buscarActivosPorPais(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoDocumentacion a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    List<TipoDocumentacion> buscarActivosPorPais(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

}

