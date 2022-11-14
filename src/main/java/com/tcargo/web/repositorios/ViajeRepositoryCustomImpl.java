package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Viaje;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ViajeRepositoryCustomImpl implements ViajeRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    private static final String AND = "' AND ";
    private static final String OR = "' OR ";

    @Override
    public Page<Viaje> buscarPorCriterios(BusquedaViajeModel busqueda, Transportador transportador, Pageable pageable, boolean isTransportador) {
        String order = "pc.finalizacion DESC";

        if (isTransportador) {
            order = "v.partida_carga_negativa";
        }

        if (pageable.getSort().isSorted()) {
            order = StringUtils.collectionToCommaDelimitedString(
                    StreamSupport.stream(pageable.getSort().spliterator(), false)
                            .map(o -> o.getProperty() + " " + o.getDirection())
                            .collect(Collectors.toList()));
        }

        String igualODistinto = isTransportador ? "='" : "!='";
        String where = where(busqueda, transportador, igualODistinto);

        String query = "SELECT v.* FROM viaje v " +
                "LEFT JOIN remolque r ON v.remolque_id = r.id " +
                "LEFT JOIN ubicacion ui ON v.ubicacion_inicial_id = ui.id " +
                "LEFT JOIN ubicacion uf ON v.ubicacion_final_id = uf.id " +
                "LEFT JOIN chofer c ON v.chofer_id = c.id " +
                "LEFT JOIN usuario usuarioChofer ON usuarioChofer.id = c.usuario_id " +
                "LEFT JOIN vehiculo_tipo_cargas vtc ON v.vehiculo_id = vtc.vehiculo_id " +
                "LEFT JOIN remolque_tipo_cargas rtc ON r.id = rtc.remolque_id " +
                "LEFT JOIN transportador t ON v.transportador_id = t.id " +
                "LEFT JOIN pedido p ON v.carga_id = p.carga_id " +
                "LEFT JOIN periodo_de_carga pc ON p.periodo_de_carga_id = pc.id " +
                "LEFT JOIN vehiculo ve ON v.vehiculo_id = ve.id " +
                "LEFT JOIN modelo m ON ve.modelo_id = m.id " +
                "LEFT JOIN tipo_remolque tr ON r.tipo_remolque_id = tr.id " +
                "WHERE " + where + " " +
                "GROUP BY v.id " +  ( pageable.getSort().isSorted() ? "ORDER BY "+order :
                "ORDER BY v.modificacion desc" );

        Query nativeQuery = em.createNativeQuery(query, Viaje.class);

        List<Viaje> viajes;
        if (pageable.isPaged()) {
            //noinspection unchecked
            viajes = nativeQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        } else {
            //noinspection unchecked
            viajes = nativeQuery.getResultList();
        }

        Query countQuery = em.createNativeQuery("SELECT COUNT(*) FROM (" + query + ") AS count");

        long count = ((BigInteger) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(viajes, pageable, count);
    }

    private String where(BusquedaViajeModel busqueda, Transportador transportador, String igualODistinto) {
        StringBuilder where = new StringBuilder("v.eliminado IS NULL AND v.carga_negativa=true AND v.estado_viaje!='FINALIZADO").append(AND);

        if (parametroPresente(busqueda.getChofer())) {
            String nombreOApellido = "%" + busqueda.getChofer() + "%";
            where.append("usuarioChofer.nombre LIKE '").append(nombreOApellido).append(AND);
        }

        if (parametroPresente(busqueda.getOrigen())) {
            where.append("ui.direccion LIKE '%").append(busqueda.getOrigen()).append("%").append(AND);
        }

        if (parametroPresente(busqueda.getDestino())) {
            where.append("uf.direccion LIKE '%").append(busqueda.getDestino()).append("%").append(AND);
        }

        if (parametroPresente(busqueda.getFechaDesde())) {
            where.append("v.partida_carga_negativa LIKE '").append(busqueda.getFechaDesde()).append("%").append(AND);
        }

        if (parametroPresente(busqueda.getFechaHasta())) {
            where.append("v.llegada_carga_negativa LIKE '").append(busqueda.getFechaHasta()).append("%").append(AND);
        }

        if (busqueda.getKms() != null && busqueda.getKms() > 0) {
            where.append("v.kms=").append(busqueda.getKms()).append(" AND ");
        }

        if (!busqueda.getTipoDeCarga().isEmpty()) {
            String tiposDeCarga = StringUtils.collectionToDelimitedString(busqueda.getTipoDeCarga(), ",", "'", "'");
            where.append("(vtc.tipo_cargas_id IN (").append(tiposDeCarga).append(") OR ")
                    .append("rtc.tipo_cargas_id IN (").append(tiposDeCarga).append(")) AND ");
        }

        if (parametroPresente(busqueda.getQ())) {
            String otro = "%" + busqueda.getQ() + "%";
            where.append("(t.nombre LIKE '").append(otro).append(OR)
                    .append("ui.direccion LIKE '").append(otro).append(OR)
                    .append("uf.direccion LIKE '").append(otro).append(OR)
                    .append("ve.dominio LIKE '").append(otro).append(OR)
                    .append("m.nombre LIKE '").append(otro).append(OR)
                    .append("usuarioChofer.nombre LIKE '").append(otro).append("') AND ");
        }

        if (parametroPresente(busqueda.getTipoDeRemolque())) {
            where.append("r.tipo_remolque_id='").append(busqueda.getTipoDeRemolque()).append(AND);
        }

        if (parametroPresente(busqueda.getTipoDeCamion())) {
            where.append("ve.tipo_vehiculo_id='").append(busqueda.getTipoDeCamion()).append(AND);
        }

        if (parametroPresente(busqueda.getIdModelo())) {
            where.append("ve.modelo_id='").append(busqueda.getIdModelo()).append(AND);
        }

        if (parametroPresente(busqueda.getVehiculo())) {
            where.append("v.vehiculo_id='").append(busqueda.getVehiculo()).append(AND);
        }

        if (transportador != null) {
            where.append("v.transportador_id").append(igualODistinto).append(transportador.getId()).append(AND);
        }

        where.delete(where.length() - 5, where.length());
        return where.toString();
    }

    private boolean parametroPresente(String parametro) {
        return parametro != null && !parametro.isEmpty();
    }

}
