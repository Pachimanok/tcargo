package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Chofer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoferRepository extends JpaRepository<Chofer, String> {

    @Query("SELECT a from Chofer a WHERE a.eliminado IS NULL")
    Page<Chofer> buscarActivos(Pageable pageable);

    @Query("select a from Chofer a WHERE a.eliminado IS NULL AND (a.usuario.nombre LIKE :q OR a.documento LIKE :q OR a.usuario.mail LIKE :q OR a.emailAdicional LIKE :q OR a.telefonoAdicional LIKE :q )")
    Page<Chofer> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from Chofer a WHERE a.eliminado IS NULL AND a.transportador.id = :id")
    Page<Chofer> buscarActivosPorTransportador(@Param("id") String id, Pageable pageable);

    @Query("SELECT c FROM Chofer c WHERE c.eliminado IS NULL AND c.transportador.id = :id AND (c.usuario.nombre LIKE :q OR c.documento LIKE :q OR c.usuario.mail LIKE :q)")
    Page<Chofer> buscarActivosPorTransportadorConQuery(Pageable pageable, @Param("id") String id, @Param("q") String q);

    @Query("SELECT a from Chofer a WHERE a.eliminado IS NULL AND (a.transportador.id LIKE :id)")
    List<Chofer> buscarActivosPorTransportadorList(@Param("id") String id);

    @Query("SELECT a from Chofer a WHERE a.eliminado IS NULL ORDER BY a.usuario.nombre")
    List<Chofer> buscarActivos();

    @Query("select a from Chofer a WHERE a.eliminado IS NOT NULL")
    Page<Chofer> listarEliminados(Pageable pageable);

    @Query("select a from Chofer a")
    Page<Chofer> listarTodos(Pageable pageable);
    
    @Query("SELECT c FROM Chofer c WHERE c.eliminado IS NULL AND c.usuario.id LIKE :id")
    Chofer buscarPorUsuarioId(@Param("id")String idUsuario);

    @Query("select a from Chofer a WHERE a.eliminado IS NULL AND a.usuario.nombre = :nombre")
    Chofer buscarPorNombre(@Param("nombre") String nombre);

    @Query("select a from Chofer a WHERE a.eliminado IS NULL AND a.id = :id")
    Chofer buscarPorId(@Param("id") String id);

    @Query("select a from Chofer a WHERE a.eliminado IS NULL AND a.documento = :documento")
    Chofer buscarPorDocumento(@Param("documento") String documento);

    @Query(value = "SELECT COUNT(chofer.id),transportador_id FROM chofer " +
            "LEFT JOIN transportador ON transportador.id LIKE chofer.transportador_id WHERE chofer.eliminado IS NULL " +
            "AND transportador.eliminado IS NULL group by chofer.transportador_id HAVING COUNT(chofer.id) = :choferes",
            nativeQuery = true)
    List<String> contarChoferesPorTransportador(@Param("choferes") Integer choferes);
    
    @Query(value = "SELECT COUNT(chofer.id) FROM chofer LEFT JOIN transportador ON transportador.id LIKE chofer.transportador_id WHERE chofer.eliminado IS NULL AND transportador.eliminado IS NULL AND transportador.id LIKE :idTransportador GROUP BY transportador.id;",
            nativeQuery = true)
    Integer contarChoferesPorTransportador(@Param("idTransportador")String idTransportador);

}

