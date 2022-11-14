package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.EstadoViaje;
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
public interface PedidoRepository extends JpaRepository<Pedido, Long>, PedidoRepositoryCustom {

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL")
    Page<Pedido> buscarActivos(Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL")
    List<Pedido> buscarActivosSinPage();

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.dador.id = :idDador")
    List<Pedido> buscarActivosPorIdDadorList(@Param("idDador") String idDador);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.carga.id = :idCarga AND p.grupo = true")
    List<Pedido> buscarActivosPorIdCarga(@Param("idCarga") String idCargar);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.ubicacionDesde = :u AND p.asignadoATransportador = false AND p.estadoPedido IN :estados " +
            "AND (p.periodoDeCarga.inicio >= :partida AND p.periodoDeCarga.inicio < :llegada OR p.periodoDeCarga.finalizacion >= :partida AND p.periodoDeCarga.finalizacion < :llegada)")
    List<Pedido> buscarParaGeoQuery(@Param("u") Ubicacion u, @Param("estados") List<EstadoPedido> estados, @Param("partida") String partida, @Param("llegada") String llegada);

    @Query(value = "SELECT COUNT(pedido.id), pedido.id FROM pedido INNER JOIN contra_oferta ON pedido.id = pedido_id GROUP BY pedido.id", nativeQuery = true)
    List<Integer> cantidadPedidosConOFertas();

    @Query(value = "SELECT COUNT(pedido.id), pedido.id FROM pedido INNER JOIN contra_oferta ON pedido.id LIKE pedido_id INNER JOIN usuario u ON pedido.dador_id LIKE u.id WHERE u.pais_id LIKE :idPais GROUP BY pedido.id", nativeQuery = true)
    List<Integer> cantidadPedidosConOFertasParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    @Query(value = "SELECT COUNT(a.id) FROM pedido a WHERE a.asignado_a_transportador = true GROUP BY a.id", nativeQuery = true)
    List<String> cantidadmatcheados();

    @Query(value = "SELECT COUNT(a.id) FROM pedido a INNER JOIN usuario u ON a.dador_id LIKE u.id WHERE a.asignado_a_transportador = true AND u.pais_id LIKE :idPais GROUP BY a.id", nativeQuery = true)
    List<String> cantidadmatcheadosParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.dador.pais.id LIKE :idPais")
    List<Pedido> buscarActivosSinPageParaAdminLocal(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.asignadoATransportador = true AND p.dador.id = :idDador")
    Page<Pedido> buscarMatcheadas(Pageable page, @Param("idDador") String idDador);

    @Query("SELECT p FROM Pedido p WHERE p.eliminado IS NULL AND p.asignadoATransportador = true AND p.dador.id = :idDador")
    List<Pedido> buscarMatcheadas(Sort sort, @Param("idDador") String idDador);

    @Query(value = "SELECT p.* FROM pedido p LEFT JOIN periodo_de_carga pc ON pc.id=p.periodo_de_carga_id WHERE p.eliminado IS NULL " +
            "AND p.asignado_a_transportador IS NULL AND p.estado_pedido=:estado AND pc.finalizacion <= :fecha AND pc.finalizacion > :hoy", nativeQuery = true)
    List<Pedido> buscarProximosAVencer(@Param("estado") Integer estadoPedido, @Param("fecha") String fecha, @Param("hoy") String hoy);

    @Query(value = "SELECT p.* FROM pedido p LEFT JOIN periodo_de_carga pc ON pc.id=p.periodo_de_carga_id WHERE p.eliminado IS NULL " +
            "AND p.estado_pedido IN :estados AND pc.finalizacion <= :hoy", nativeQuery = true)
    List<Pedido> buscarVencidos(@Param("estados") List<Integer> estados, @Param("hoy") String hoy);

    @Query(value = "SELECT p.* FROM viaje v LEFT JOIN pedido p ON v.carga_id=p.carga_id LEFT JOIN periodo_de_carga pc ON pc.id=p.periodo_de_carga_id WHERE p.eliminado IS NULL AND p.estado_pedido IN :estadosPedido AND pc.finalizacion <= :hoy AND v.estado_viaje IN :estadosViaje", nativeQuery = true)
    List<Pedido> buscarVencidos(@Param("estadosPedido") List<Integer> estadosPedido, @Param("hoy") String hoy, @Param("estadosViaje") List<String> estadosViaje);



}
