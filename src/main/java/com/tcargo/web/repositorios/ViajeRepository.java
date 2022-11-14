package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Carga;
import com.tcargo.web.entidades.Viaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, String>, ViajeRepositoryCustom {

    List<Viaje> findByEliminadoNull();

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL")
    Page<Viaje> buscarActivos(Pageable pageable);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL ORDER BY a.modificacion")
    List<Viaje> buscarActivos();

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true ORDER BY a.modificacion")
    Page<Viaje> buscarActivosCargaNegativa(Pageable page);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id LIKE :transportadorId ORDER BY a.modificacion")
    Page<Viaje> buscarActivosCargaNegativaPorTransportadorId(Pageable page, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id LIKE :transportadorId")
    List<Viaje> listarActivosCargaNegativaPorTransportadorId(@Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id NOT LIKE :transportadorId ORDER BY a.modificacion")
    Page<Viaje> buscarActivosCargaNegativaPorTransportadorIdNotLike(Pageable page, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true ORDER BY a.modificacion")
    List<Viaje> buscarActivosCargaNegativaList();

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.usuario.pais.id LIKE :idPais")
    List<Viaje> buscarActivosCargaNegativaListParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q)")
    Page<Viaje> busquedaQ(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id LIKE :transportadorId AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q)")
    Page<Viaje> busquedaQAndTransportadorId(Pageable pageable, @Param("q") String q, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id NOT LIKE :transportadorId AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q)")
    Page<Viaje> busquedaQAndTransportadorIdNotLike(Pageable pageable, @Param("q") String q, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q) AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> busquedaQAndDate(Pageable pageable, @Param("q") String q, @Param("first") Date first, @Param("second") Date second);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id LIKE :transportadorId AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q) AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> busquedaQAndDateAndTransportadorId(Pageable pageable, @Param("q") String q, @Param("first") Date first, @Param("second") Date second, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id NOT LIKE :transportadorId AND (a.vehiculo.modelo.nombre LIKE :q OR a.remolque.tipoRemolque.caracteristicas LIKE :q OR a.ubicacionInicial.direccion LIKE :q OR a.ubicacionFinal.direccion LIKE :q) AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> busquedaQAndDateAndTransportadorIdNotLike(Pageable pageable, @Param("q") String q, @Param("first") Date first, @Param("second") Date second, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarEntreFechasInicioYFinal(Pageable paginable, @Param("first") Date first, @Param("second") Date second);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id LIKE :transportadorId AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarEntreFechasInicioYFinalAndTransportadorId(Pageable paginable, @Param("transportadorId") String transportadorId, @Param("first") Date first, @Param("second") Date second);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND a.transportador.id NOT LIKE :transportadorId AND (a.partidaCargaNegativa >= :first) AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarEntreFechasInicioYFinalAndTransportadorIdNotLike(Pageable paginable, @Param("transportadorId") String transportadorId, @Param("first") Date first, @Param("second") Date second);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND (a.partidaCargaNegativa >= :first)")
    Page<Viaje> buscarFechaInicioEnAdelante(Pageable paginable, @Param("first") Date first);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.transportador.id LIKE :transportadorId AND a.cargaNegativa = true AND (a.partidaCargaNegativa >= :first)")
    Page<Viaje> buscarFechaInicioEnAdelanteAndTransportadorId(Pageable paginable, @Param("first") Date first, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.transportador.id NOT LIKE :transportadorId AND a.cargaNegativa = true AND (a.partidaCargaNegativa >= :first)")
    Page<Viaje> buscarFechaInicioEnAdelanteAndTransportadorIdNotLike(Pageable paginable, @Param("first") Date first, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarFechaFinalHaciaAtras(Pageable paginable, @Param("second") Date second);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true  AND a.transportador.id LIKE :transportadorId AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarFechaFinalHaciaAtrasAndTransportadorId(Pageable paginable, @Param("second") Date second, @Param("transportadorId") String transportadorId);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL AND a.cargaNegativa = true  AND a.transportador.id NOT LIKE :transportadorId AND (a.llegadaCargaNegativa <= :second)")
    Page<Viaje> buscarFechaFinalHaciaAtrasAndTransportadorIdNotLike(Pageable paginable, @Param("second") Date second, @Param("transportadorId") String transportadorId);

    @Query(value = "SELECT COUNT(v.chofer_id) FROM viaje v LEFT JOIN chofer c ON c.id LIKE v.chofer_id WHERE c.eliminado IS NULL AND v.eliminado IS NULL AND c.id LIKE :idChofer AND v.estado_viaje NOT LIKE 'FINALIZADO' GROUP BY v.chofer_id;", nativeQuery = true)
    Integer cantidadDeViajesPorChofer(@Param("idChofer") String idChofer);

    @Query("SELECT v FROM Viaje v WHERE v.chofer.id LIKE :idChofer")
    List<Viaje> listarPorChofer(@Param("idChofer") String idChofer);

    Viaje findByEliminadoNullAndCarga(Carga carga);

}
	