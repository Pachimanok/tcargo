package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface CoincidenciaRepository extends JpaRepository<Coincidencia, String> {

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL")
    Page<Coincidencia> buscarActivos(Pageable pageable);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL")
    List<Coincidencia> buscarActivos(Sort sort);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND (p.detalle LIKE :q OR p.viaje.vehiculo.modelo.nombre LIKE :q OR p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q OR p.viaje.chofer.usuario.nombre LIKE :q)")
    Page<Coincidencia> buscarActivosQ(Pageable pageable, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND (p.detalle LIKE :q OR p.viaje.vehiculo.modelo.nombre LIKE :q OR p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q OR p.viaje.chofer.usuario.nombre LIKE :q)")
    List<Coincidencia> buscarActivosQ(Sort sort, @Param("q") String q);

    Page<Coincidencia> findByCostoAndEliminadoIsNull(Pageable page, Double costo);

    List<Coincidencia> findByCostoAndEliminadoIsNull(Sort sort, Double costo);

    Page<Coincidencia> findByCostoAndPedidoDadorPaisIdAndEliminadoIsNull(Pageable page, Double costo, String paisId);

    List<Coincidencia> findByCostoAndPedidoDadorPaisIdAndEliminadoIsNull(Sort sort, Double costo, String paisId);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.dador.pais.id = :idPais")
    Page<Coincidencia> buscarActivosPorPais(Pageable pageable, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.dador.pais.id = :idPais")
    List<Coincidencia> buscarActivosPorPais(Sort sort, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT p FROM Coincidencia p WHERE (p.eliminado IS NULL) AND (p.pedido.dador.pais.id = :idPais) AND (p.detalle LIKE :q) OR (p.viaje.vehiculo.modelo.nombre LIKE :q) OR (p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q) OR (p.viaje.chofer.usuario.nombre LIKE :q)")
    Page<Coincidencia> buscarActivosPorPaisQ(Pageable pageable, @Param(ID_PAIS_LABEL) String idPais, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE (p.eliminado IS NULL) AND (p.pedido.dador.pais.id = :idPais) AND (p.detalle LIKE :q) OR (p.viaje.vehiculo.modelo.nombre LIKE :q) OR (p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q) OR (p.viaje.chofer.usuario.nombre LIKE :q)")
    List<Coincidencia> buscarActivosPorPaisQ(Sort sort, @Param(ID_PAIS_LABEL) String idPais, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :idTransportador")
    Page<Coincidencia> buscarActivosPorIdTransportador(Pageable pageable, @Param("idTransportador") String idTransportador);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :id")
    List<Coincidencia> buscarActivosPorIdTransportadorOrdenados(Sort sort, @Param("id") String id);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :id AND (p.costo = :q OR p.detalle LIKE :q OR p.viaje.vehiculo.modelo.nombre LIKE :q OR p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q OR p.viaje.chofer.usuario.nombre LIKE :q)")
    List<Coincidencia> buscarActivosPorIdTransportadorOrdenados(Sort sort, @Param("id") String id, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :idTransportador AND p.pedido.periodoDeCarga.inicio >= :hoy AND p.pedido.carga.cargaCompleta = false AND p.asignadoAViajePersonal IS NULL")
    List<Coincidencia> buscarActivosPorIdTransportadorNoVencidas(@Param("idTransportador") String idTransportador, @Param("hoy") String hoy);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :idTransportador AND (p.costo = :q OR p.detalle LIKE :q OR p.viaje.vehiculo.modelo.nombre LIKE :q OR p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q OR p.viaje.chofer.usuario.nombre LIKE :q)")
    Page<Coincidencia> buscarActivosPorIdTransportadorQ(Pageable pageable, @Param("idTransportador") String idTransportador, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :idTransportador AND (p.costo = :q OR p.detalle LIKE :q OR p.viaje.vehiculo.modelo.nombre LIKE :q OR p.viaje.remolque.tipoRemolque.caracteristicas LIKE :q OR p.viaje.chofer.usuario.nombre LIKE :q)")
    List<Coincidencia> buscarActivosPorIdTransportadorList(Pageable pageable, @Param("idTransportador") String idTransportador, @Param("q") String q);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.transportador.id = :idTransportador")
    List<Coincidencia> buscarActivosPorIdTransportadorList(@Param("idTransportador") String idTransportador);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.id = :idPedido")
    List<Coincidencia> buscarPorIdpedido(@Param("idPedido") Long idPedido);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.id = :idPedido")
    Coincidencia buscarPorIdpedidoEntidad(@Param("idPedido") Long idPedido);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.id = :idPedido")
    Coincidencia getPorIdpedido(@Param("idPedido") Long idPedido);

    @Query("SELECT p FROM Coincidencia p WHERE p.eliminado IS NULL AND p.pedido.dador.id LIKE :idDador")
    List<Coincidencia> buscarPorDador(@Param("idDador") String idDador);

    @Query("SELECT p FROM Coincidencia p WHERE p.pedido.dador.id LIKE :idDador AND p.notificacion = :notificacion AND p.eliminado IS NULL")
    List<Coincidencia> buscaPordadorNotificacionCreada(@Param("idDador") String idDador, @Param("notificacion") EstadoNotificacion notificacion);

    @Query("SELECT p FROM Coincidencia p WHERE p.pedido.dador.id LIKE :idDador AND p.notificacionConformidad = :notificacion AND p.eliminado IS NULL AND p.viaje.estadoViaje = 'FINALIZADO'")
    List<Coincidencia> buscaPordadorNotificacionConformidadCreada(@Param("idDador") String idDador, @Param("notificacion") EstadoNotificacion notificacion);

    @Query(value = "SELECT m.nombre, SUM(c.costo) as \"sumas\" FROM coincidencia c INNER JOIN pedido p ON c.pedido_id LIKE p.id INNER JOIN moneda m ON m.id LIKE p.moneda_id WHERE p.eliminado IS NULL AND p.dador_id LIKE :idDador group by m.id;", nativeQuery = true)
    List<Map<String, Double>> sumasPorTipodeMonedasDador(@Param("idDador") String idDador);

    @Query(value = "SELECT m.nombre, SUM(c.costo) as \"sumas\" FROM coincidencia c INNER JOIN pedido p ON c.pedido_id LIKE p.id INNER JOIN moneda m ON m.id LIKE p.moneda_id WHERE p.eliminado IS NULL AND c.transportador_id LIKE :idTransportador group by m.id;", nativeQuery = true)
    List<Map<String, Double>> sumasPorTipodeMonedasTransportador(@Param("idTransportador") String idTransportador);

    @Query("SELECT c From Coincidencia c WHERE c.eliminado IS NULL AND c.viaje.chofer.id LIKE :idChofer")
    List<Coincidencia> buscarPorChoferEnViaje(@Param("idChofer") String idChofer);

    @Query("SELECT c FROM Coincidencia c WHERE c.pedido.periodoDeCarga.inicio LIKE :fecha")
    List<Coincidencia> buscarProximosAIniciar(@Param("fecha") String fecha);

}
