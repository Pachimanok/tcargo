package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Remolque;
import com.tcargo.web.entidades.TipoCarga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemolqueRepository extends JpaRepository<Remolque, String> {

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NULL")
    Page<Remolque> buscarActivos(Pageable pageable);

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NULL AND (a.transportador.id LIKE :id)")
    Page<Remolque> buscarActivosPorTransportador(@Param("id") String id, Pageable pageable);

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NULL AND (a.transportador.id LIKE :id)")
    List<Remolque> buscarActivosPorTransportadorList(@Param("id") String id);

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NULL ORDER BY a.tipoRemolque.caracteristicas")
    List<Remolque> buscarActivos();

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NOT NULL")
    Page<Remolque> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NOT NULL AND (a.dominio LIKE :dominio)")
    Remolque buscarPorDominio(@Param("dominio") String dominio);

    @Query("SELECT a from Remolque a")
    Page<Remolque> buscarTodos(Pageable pageable);

    @Query("SELECT a from Remolque a WHERE a.eliminado IS NULL AND (a.tipoRemolque.caracteristicas LIKE :nombre OR a.dominio LIKE :nombre OR a.anioFabricacion LIKE :nombre)")
    Page<Remolque> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Remolque a WHERE a.tipoCargas IN :tipoCargas")
    List<Remolque> contarCargasPorTransportador(@Param("tipoCargas") TipoCarga tipoCargas);

    List<Remolque> findDistinctBytipoCargasInAndEliminadoIsNull(List<TipoCarga> tp);

    Remolque findByDominio(String dominio);

}

