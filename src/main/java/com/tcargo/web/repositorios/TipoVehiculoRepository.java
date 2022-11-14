package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.TipoVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, String> {

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL")
    Page<TipoVehiculo> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<TipoVehiculo> buscarActivos();

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NOT NULL")
    Page<TipoVehiculo> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoVehiculo a")
    Page<TipoVehiculo> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL")
    List<TipoVehiculo> buscarActivos(Sort sort);

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL AND (a.nombre LIKE :q OR a.descripcion LIKE :q )")
    Page<TipoVehiculo> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL AND (a.nombre LIKE :q OR a.descripcion LIKE :q )")
    List<TipoVehiculo> buscarActivos(Sort sort, @Param("q") String q);

    @Query("SELECT a from TipoVehiculo a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<TipoVehiculo> buscarTipoVehiculoPorNombre(@Param("nombre") String nombre);

}

