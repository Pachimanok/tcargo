package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Marca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, String> {

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<Marca> buscarActivos();

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL")
    Page<Marca> buscarActivos(Pageable pageable);

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL")
    List<Marca> buscarActivos(Sort sort);

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre ")
    Page<Marca> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    List<Marca> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from Marca a WHERE a.eliminado IS NOT NULL")
    Page<Marca> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Marca a")
    Page<Marca> buscarTodos(Pageable pageable);

    @Query("SELECT a from Marca a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<Marca> buscarMarcaPorNombre(@Param("nombre") String nombre);

}

