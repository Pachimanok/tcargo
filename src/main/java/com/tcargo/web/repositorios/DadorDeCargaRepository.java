package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.DadorDeCarga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DadorDeCargaRepository extends JpaRepository<DadorDeCarga, String> {

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NULL")
    Page<DadorDeCarga> buscarActivos(Pageable pageable);

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<DadorDeCarga> buscarActivos();

    List<DadorDeCarga> findByRazonSocial(String razonSocial);

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NOT NULL")
    Page<DadorDeCarga> buscarEliminados(Pageable pageable);

    @Query("SELECT a from DadorDeCarga a")
    Page<DadorDeCarga> buscarTodos(Pageable pageable);

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre)")
    Page<DadorDeCarga> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<DadorDeCarga> buscarDadorDeCargaPorNombre(@Param("nombre") String nombre);

    @Query("SELECT a from DadorDeCarga a WHERE a.eliminado IS NULL AND a.usuario.id = :id")
    DadorDeCarga buscarDadorDeCargaPorIdUsuario(@Param("id") String id);

    @Query("SELECT a from DadorDeCarga a, IN(a.viajes) t WHERE a.eliminado IS NULL AND t.id = :idViaje")
    DadorDeCarga buscarDadorDeCargaPorViaje(@Param("idViaje") String idViaje);

    @Query("SELECT a from DadorDeCarga a WHERE a.usuario.id = :id")
    DadorDeCarga buscarDadorDeCargaEliminadoPorIdUsuario(@Param("id") String id);

}

