package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.entidades.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, String>, VehiculoRepositoryCustom {

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL")
    Page<Vehiculo> buscarActivos(Pageable pageable);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL AND (a.transportador.id LIKE :id)")
    Page<Vehiculo> buscarActivosPorTransportador(@Param("id") String id, Pageable pageable);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL AND (a.transportador.id LIKE :id)")
    List<Vehiculo> buscarActivosPorTransportadorList(@Param("id") String id);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL ORDER BY a.modelo.nombre")
    List<Vehiculo> buscarActivos();

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NOT NULL")
    Page<Vehiculo> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NOT NULL AND (a.dominio LIKE :dominio)")
    Vehiculo buscarPorDominio(@Param("dominio") String dominio);

    @Query("SELECT a from Vehiculo a")
    Page<Vehiculo> buscarTodos(Pageable pageable);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL AND (a.modelo.nombre LIKE :nombre OR a.modelo.marca.nombre LIKE :nombre OR a.tipoVehiculo.nombre LIKE :nombre OR a.dominio LIKE :nombre OR a.anioFabricacion LIKE :nombre)")
    Page<Vehiculo> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Vehiculo a WHERE a.eliminado IS NULL AND a.modelo.nombre = :nombre")
    List<Vehiculo> buscarVehiculoPorNombre(@Param("nombre") String nombre);

    @Query(value = "SELECT COUNT(tv.id), tv.nombre FROM vehiculo v INNER JOIN tipo_vehiculo tv ON v.tipo_vehiculo_id LIKE tv.id GROUP BY tv.id", nativeQuery = true)
    List<String> cantidadVehiculosPorTipo();

    @Query(value = "SELECT COUNT(tv.id), tv.nombre FROM vehiculo v INNER JOIN tipo_vehiculo tv ON v.tipo_vehiculo_id LIKE tv.id INNER JOIN transportador t ON v.transportador_id LIKE t.id INNER JOIN usuario u ON t.usuario_id LIKE u.id WHERE u.pais_id LIKE :idPais GROUP BY tv.id", nativeQuery = true)
    List<String> cantidadVehiculosPorTipoParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    List<Vehiculo> findDistinctBytipoCargasInAndEliminadoIsNull(List<TipoCarga> tp);

    Vehiculo findByDominio(String dominio);

}

