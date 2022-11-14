package com.tcargo.web.repositorios;


import com.tcargo.web.entidades.Localidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, String> {

    @Query("SELECT a from Localidad a WHERE a.eliminado IS NULL")
    public Page<Localidad> buscarActivos(Pageable pageable);

    @Query("SELECT a from Localidad a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    public List<Localidad> buscarActivos();

    @Query("SELECT a from Localidad a WHERE a.eliminado IS NOT NULL")
    public Page<Localidad> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Localidad a")
    public Page<Localidad> buscarTodos(Pageable pageable);

    @Query("SELECT a from Localidad a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.provincia.nombre LIKE :nombre)")
    public Page<Localidad> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Localidad a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    public List<Localidad> buscarLocalidadPorNombre(@Param("nombre") String nombre);
}

