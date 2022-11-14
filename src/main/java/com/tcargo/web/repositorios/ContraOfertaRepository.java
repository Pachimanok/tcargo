package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContraOfertaRepository extends JpaRepository<ContraOferta, String>, ContraOfertaRepositoryCustom {

    @Query("SELECT a from ContraOferta a WHERE a.eliminado IS NULL ORDER BY a.pedido.id DESC")
    Page<ContraOferta> buscarActivos(Pageable pageable);

    @Query("SELECT a from ContraOferta a WHERE a.eliminado IS NULL ORDER BY a.pedido.id DESC")
    List<ContraOferta> buscarActivos();

    @Query("SELECT a from ContraOferta a WHERE a.eliminado IS NOT NULL")
    Page<ContraOferta> buscarEliminados(Pageable pageable);

    List<ContraOferta> findByTransportadorAndNotificacionAndCreadorNotLikeAndEstadoAndEliminadoIsNull(Transportador t, EstadoNotificacion notificacion, Usuario creador, EstadoContraOferta estado);

    List<ContraOferta> findByTransportadorAndNotificacionAndCreadorAndEstadoAndEliminadoIsNull(Transportador t, EstadoNotificacion notificacion, Usuario creador, EstadoContraOferta estado);

    List<ContraOferta> findByDadorAndNotificacionAndCreadorNotLikeAndEstadoAndEliminadoIsNull(Usuario dador, EstadoNotificacion notificacion, Usuario creador, EstadoContraOferta estado);

    List<ContraOferta> findByDadorAndCreadorNotLikeAndEstadoAndEliminadoIsNullAndPedidoAsignadoATransportadorIsNull(Usuario dador, Usuario creador, EstadoContraOferta estado);

    List<ContraOferta> findByDadorAndNotificacionAndCreadorAndEstadoAndEliminadoIsNull(Usuario dador, EstadoNotificacion notificacion, Usuario creador, EstadoContraOferta estado);

    List<ContraOferta> findByTransportadorAndEliminadoIsNull(Transportador transportador);

    List<ContraOferta> findDistinctPedidoIdByEstadoAndEliminadoIsNull(EstadoContraOferta estado);

    List<ContraOferta> findByPedidoAndEstadoAndDadorAndCreadorNotLikeAndEliminadoIsNull(Pedido pedido, EstadoContraOferta estado, Usuario dador, Usuario creador);

    List<ContraOferta> findByPedidoAndEstadoAndTransportadorAndCreadorNotLikeAndEliminadoIsNull(Pedido pedido, EstadoContraOferta estado, Transportador transportador, Usuario creador);

    @Query("SELECT a from ContraOferta a")
    Page<ContraOferta> buscarTodos(Pageable pageable);

    @Query("SELECT a FROM ContraOferta a WHERE a.eliminado IS NULL AND a.pedido.id = :idPedido ORDER BY a.modificacion ASC")
    List<ContraOferta> buscarContraOfertasPorIdPedido(@Param("idPedido") Long idPedido);

    @Query("SELECT c FROM ContraOferta c WHERE (c.pedido.id = :idPedido) AND (c.valor = :valor) AND (c.transportador.usuario.nombre LIKE :q OR c.transportador.nombre LIKE :q OR c.dador.nombre LIKE :q OR c.vehiculo.modelo.nombre LIKE :q OR c.pedido.ubicacionDesde.direccion LIKE :q OR c.pedido.ubicacionHasta.direccion LIKE :q OR c.estado = :q) AND c.eliminado IS NULL")
    List<ContraOferta> busqueda(@Param("idPedido") Long idPedido, @Param("valor") Double valor, @Param("q") String q);

    @Query(value = "SELECT COUNT(co.id), co.creador_id FROM contra_oferta co LEFT JOIN transportador t ON co.transportador_id = t.id LEFT JOIN usuario u ON co.creador_id = u.id WHERE co.eliminado IS NULL AND u.eliminado IS NULL AND co.transportador_id = t.id AND co.creador_id = u.id GROUP BY co.creador_id HAVING COUNT(co.creador_id) = :numero", nativeQuery = true)
    List<String> contarPorCreador(@Param("numero") Integer numero);

    @Query(value = "SELECT COUNT(co.pedido_id) FROM contra_oferta co INNER JOIN transportador t ON co.transportador_id = t.id INNER JOIN usuario u ON co.creador_id = u.id WHERE co.eliminado IS NULL AND u.id LIKE :idCreador GROUP BY co.creador_id", nativeQuery = true)
    Integer contarParaReporteTransportador(@Param("idCreador") String idCreador);

    @Query(value = "SELECT COUNT(co.id) FROM contra_oferta co WHERE co.eliminado IS NULL AND co.creador_id NOT LIKE :idCreador AND co.dador_id LIKE :idCreador", nativeQuery = true)
    Integer contarComoDadorNoPropias(@Param("idCreador") String idCreador);

    @Query("select co from ContraOferta co where co.eliminado is null and co.pedido.id = :idPedido and co.transportador.id = :idTransportador")
    List<ContraOferta> buscarPorIdPedidoYIdTransportador(@Param("idPedido") Long idPedido, @Param("idTransportador") String idTransportador);

}

