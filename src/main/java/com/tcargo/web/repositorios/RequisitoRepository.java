package com.tcargo.web.repositorios;


import com.tcargo.web.entidades.Requisito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RequisitoRepository extends JpaRepository<Requisito, String> {

    @Query("SELECT a from Requisito a WHERE a.eliminado IS NULL")
    public Page<Requisito> buscarActivos(Pageable pageable);

    @Query("SELECT a from Requisito a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    public List<Requisito> buscarActivos();

    @Query("SELECT a from Requisito a WHERE a.eliminado IS NOT NULL")
    public Page<Requisito> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Requisito a")
    public Page<Requisito> buscarTodos(Pageable pageable);

    @Query("SELECT a from Requisito a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    public Page<Requisito> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Requisito a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    public List<Requisito> buscarRequisitoPorNombre(@Param("nombre") String nombre);
}

