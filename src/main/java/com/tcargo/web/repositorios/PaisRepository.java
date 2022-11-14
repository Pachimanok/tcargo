package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Pais;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaisRepository extends JpaRepository<Pais, String> {

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<Pais> buscarActivos();

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL")
    Page<Pais> buscarActivos(Pageable pageable);

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL")
    List<Pais> buscarActivos(Sort sort);

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    Page<Pais> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    List<Pais> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from Pais a WHERE a.eliminado IS NOT NULL")
    Page<Pais> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Pais a")
    Page<Pais> buscarTodos(Pageable pageable);

    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<Pais> buscarPaisPorNombre(@Param("nombre") String nombre);
    
    @Query("SELECT a from Pais a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    public Pais buscarPorNombre(@Param("nombre") String nombre);

}

