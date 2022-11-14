package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
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
public interface TransportadorRepository extends JpaRepository<Transportador, String>, TransportadorRepositoryCustom {

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL")
    Page<Transportador> buscarActivos(Pageable pageable);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.usuario.pais.id LIKE :idPais)")
    Page<Transportador> buscarActivosPorPais(Pageable pageable, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.usuario.pais.id LIKE :idPais)")
    List<Transportador> buscarActivosPorPais(Sort sort, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.usuario.pais.id LIKE :idPais)")
    List<Transportador> buscarActivosPorPaisList(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND a.nombre IS NOT NULL ORDER BY a.nombre")
    List<Transportador> buscarActivos();

    List<Transportador> findByRazonSocial(String razonSocial);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NOT NULL")
    Page<Transportador> buscarEliminados(Pageable pageable);

    @Query("SELECT a from Transportador a")
    Page<Transportador> buscarTodos(Pageable pageable);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.usuario.mail LIKE :nombre OR a.usuario.ubicacion.direccion LIKE :nombre OR a.usuario.telefono LIKE :nombre)")
    Page<Transportador> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.usuario.mail LIKE :nombre OR a.usuario.ubicacion.direccion LIKE :nombre OR a.usuario.telefono LIKE :nombre) AND (a.usuario.pais.id LIKE :idPais)")
    Page<Transportador> buscarActivos(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.usuario.mail LIKE :nombre OR a.usuario.ubicacion.direccion LIKE :nombre OR a.usuario.telefono LIKE :nombre) AND (a.usuario.pais.id LIKE :idPais)")
    List<Transportador> buscarActivos(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a FROM Transportador a WHERE a.usuario IS :usuario AND a.eliminado IS NULL")
    Transportador buscarPorUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT a from Transportador a WHERE a.eliminado IS NULL AND a.usuario.id = :id")
    Transportador buscarTransportadorPorIdUsuario(@Param("id") String id);

    @Query(value = "SELECT COUNT(v.id), t.nombre FROM transportador t INNER JOIN vehiculo v ON t.id LIKE v.transportador_id GROUP BY t.id", nativeQuery = true)
    List<String> flotaTotalPorTransportador();

    @Query(value = "SELECT COUNT(v.id), t.nombre FROM transportador t INNER JOIN vehiculo v ON t.id LIKE v.transportador_id INNER JOIN usuario u ON t.usuario_id LIKE u.id WHERE u.pais_id LIKE :idPais GROUP BY t.id", nativeQuery = true)
    List<String> flotaTotalPorTransportadorParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    Transportador findByViajesId(String id);

}
