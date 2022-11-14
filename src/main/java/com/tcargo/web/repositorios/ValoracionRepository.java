package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Valoracion;
import com.tcargo.web.enumeraciones.EstadoValoracion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValoracionRepository extends JpaRepository<Valoracion, String> {

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.estadoValoracion = :estadoValoracion")
    List<Valoracion> buscarPorEstadoValoracion(@Param("estadoValoracion") EstadoValoracion estadoValoracion);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.estadoValoracion = :estadoValoracion AND v.creador.id LIKE :idCreador")
    List<Valoracion> buscarPropiasParaCompletar(@Param("estadoValoracion") EstadoValoracion estadoValoracion, @Param("idCreador") String idCreador);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.estadoValoracion = :estadoValoracion AND v.receptor.id LIKE :idReceptor")
    List<Valoracion> buscarPropiasComoReceptor(@Param("estadoValoracion") EstadoValoracion estadoValoracion, @Param("idReceptor") String idReceptor);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.creador.id LIKE :idCreador")
    Page<Valoracion> buscarPorIdCreador(Pageable page, @Param("idCreador") String idCreador);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.creador.id LIKE :idCreador AND v.coincidencia.pedido.id = :idPedido")
    Valoracion buscarPorIdCreadorAndPedido(@Param("idCreador") String idCreador, @Param("idPedido") Long idPedido);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.receptor.id LIKE :idReceptor AND v.comentarios IS NOT NULL AND v.valoracion IS NOT NULL")
    Page<Valoracion> buscarPorIdReceptor(Pageable page, @Param("idReceptor") String idReceptor);

    @Query(value = "SELECT ROUND(AVG(v.valoracion)) FROM valoracion v WHERE v.eliminado IS NULL AND v.receptor_id LIKE :idReceptor AND v.comentarios IS NOT NULL AND v.valoracion IS NOT NULL", nativeQuery = true)
    Integer promedioValoraciones(@Param("idReceptor") String idReceptor);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.creador.id=:idCreador ORDER BY v.modificacion DESC")
    List<Valoracion> buscarPorIdCreadorList(@Param("idCreador") String idCreador);

    @Query("SELECT v FROM Valoracion v WHERE v.eliminado IS NULL AND v.receptor.id=:idReceptor ORDER BY v.modificacion DESC")
    List<Valoracion> buscarPorIdReceptorList(@Param("idReceptor") String idReceptor);

}
