package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Modelo;
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
public interface ModeloRepository extends JpaRepository<Modelo, String> {

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL ORDER BY m.nombre")
    List<Modelo> buscarActivos();

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL")
    Page<Modelo> buscarActivos(Pageable pageable);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL")
    List<Modelo> buscarActivos(Sort sort);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND (m.nombre LIKE :nombre OR m.marca.nombre LIKE :nombre)")
    Page<Modelo> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND (m.nombre LIKE :nombre OR m.marca.nombre LIKE :nombre)")
    List<Modelo> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NOT NULL")
    Page<Modelo> buscarEliminados(Pageable pageable);

    @Query("SELECT m from Modelo m")
    Page<Modelo> buscarTodos(Pageable pageable);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.nombre = :nombre")
    List<Modelo> buscarModeloPorNombre(@Param("nombre") String nombre);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.nombre LIKE :nombre AND m.pais.id LIKE :idPais")
    Page<Modelo> buscarActivosPorPais(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.nombre LIKE :nombre AND m.pais.id LIKE :idPais")
    List<Modelo> buscarActivosPorPais(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.pais.id LIKE :idPais")
    Page<Modelo> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.pais.id LIKE :idPais")
    List<Modelo> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT m from Modelo m WHERE m.eliminado IS NULL AND m.pais.id LIKE :idPais")
    List<Modelo> buscarActivosPorPais(String idPais);
}

