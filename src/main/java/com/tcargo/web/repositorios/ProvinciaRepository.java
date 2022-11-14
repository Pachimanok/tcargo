package com.tcargo.web.repositorios;


import com.tcargo.web.entidades.Provincia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, String> {

    @Query("SELECT a from Provincia a WHERE a.eliminado IS NULL")
    public Page<Provincia> buscarActivos(Pageable pageable);

    @Query("SELECT a from Provincia a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    public List<Provincia> buscarActivos();

    @Query("SELECT a from Provincia a WHERE a.eliminado IS NOT NULL")
    public Page<Provincia> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Provincia a")
    public Page<Provincia> buscarTodos(Pageable pageable);

    @Query("SELECT a from Provincia a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.pais.nombre LIKE :nombre)")
    public Page<Provincia> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Provincia a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    public List<Provincia> buscarProvinciaPorNombre(@Param("nombre") String nombre);
}

