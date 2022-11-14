package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Documentacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentacionRepository extends JpaRepository<Documentacion, String> {

    @Query("SELECT a from Documentacion a WHERE a.eliminado IS NULL")
    Page<Documentacion> buscarActivos(Pageable pageable);

    @Query("SELECT a from Documentacion a WHERE a.eliminado IS NULL ORDER BY a.tipoDocumentacion.nombre")
    List<Documentacion> buscarActivos();

    @Query("SELECT a from Documentacion a WHERE a.eliminado IS NOT NULL")
    Page<Documentacion> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Documentacion a")
    Page<Documentacion> buscarTodos(Pageable pageable);

    @Query("SELECT a from Documentacion a WHERE a.eliminado IS NULL AND (a.tipoDocumentacion.nombre LIKE :nombre)")
    Page<Documentacion> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Documentacion a WHERE a.eliminado IS NULL AND a.tipoDocumentacion.nombre = :nombre")
    List<Documentacion> buscarDocumentacionPorNombre(@Param("nombre") String nombre);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.chofer.id = :id")
    Page<Documentacion> buscarPorIdChofer(Pageable pageable, @Param("id") String idChofer);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.chofer.id = :id AND d.titulo LIKE :q")
    Page<Documentacion> buscarPorIdChoferConQuery(Pageable pageable, @Param("id") String idChofer, @Param("q") String q);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.vehiculo.id = :id")
    Page<Documentacion> buscarPorIdVehiculo(Pageable pageable, @Param("id") String idVehiculo);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.vehiculo.id = :id AND d.titulo LIKE :q")
    Page<Documentacion> buscarPorIdVehiculoConQuery(Pageable pageable, @Param("id") String idVehiculo, @Param("q") String q);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.remolque.id = :id")
    Page<Documentacion> buscarPorIdRemolque(Pageable pageable, @Param("id") String idRemolque);

    @Query("SELECT d FROM Documentacion d WHERE d.eliminado IS NULL AND d.remolque.id = :id AND d.titulo LIKE :q")
    Page<Documentacion> buscarPorIdRemolqueConQuery(Pageable pageable, @Param("id") String idRemolque, @Param("q") String q);

}

