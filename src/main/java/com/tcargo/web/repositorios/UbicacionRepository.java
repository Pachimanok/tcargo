package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Ubicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, String>, CustomizedUbicacionRepository {

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NULL AND a.isMasterPoint = true")
    Page<Ubicacion> buscarActivos(Pageable pageable);

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NULL AND a.isMasterPoint = true ORDER BY a.direccion")
    List<Ubicacion> buscarActivos();

    @Query("SELECT a from Ubicacion a WHERE (a.eliminado IS NULL) AND (a.isMasterPoint = true) AND (a.pais.id LIKE :idPais)")
    Page<Ubicacion> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NULL AND (a.isMasterPoint = true) AND (a.direccion LIKE :direccion) AND (a.pais.id LIKE :idPais)")
    Page<Ubicacion> buscarActivosPorPais(Pageable pageable, @Param("direccion") String direccion, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NOT NULL AND a.isMasterPoint = true")
    Page<Ubicacion> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Ubicacion a")
    Page<Ubicacion> buscarTodos(Pageable pageable);

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NULL AND a.isMasterPoint = true AND (a.direccion LIKE :direccion)")
    Page<Ubicacion> buscarActivos(Pageable pageable, @Param("direccion") String direccion);

    @Query("SELECT a from Ubicacion a WHERE a.eliminado IS NULL AND a.isMasterPoint = true AND a.direccion = :direccion")
    List<Ubicacion> buscarUbicacionPorDireccion(@Param("direccion") String direccion);

}

