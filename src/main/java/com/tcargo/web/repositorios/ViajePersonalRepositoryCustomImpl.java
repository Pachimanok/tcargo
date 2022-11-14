package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.ViajePersonal;
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
public class ViajePersonalRepositoryCustomImpl implements ViajePersonalRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String AND = "' AND ";

    @Override
    public Page<ViajePersonal> buscarPorCriterios(BusquedaViajeModel busqueda, Transportador transportador, Pageable pageable, boolean isTransportador) {
        String order = "vp.fecha_inicio";

        if (pageable.getSort().isSorted()) {
            order = StringUtils.collectionToCommaDelimitedString(
                    StreamSupport.stream(pageable.getSort().spliterator(), false)
                            .map(o -> o.getProperty() + " " + o.getDirection())
                            .collect(Collectors.toList())); 
        }
        
        String igualODistinto = isTransportador ? "='" : "!='";
        String queryString = "SELECT vp.* FROM viaje_personal vp " +
                "LEFT JOIN remolque r ON vp.remolque_id = r.id " +
                "LEFT JOIN tipo_remolque tr ON r.tipo_remolque_id = tr.id " +
                "LEFT JOIN vehiculo v ON vp.vehiculo_id = v.id " +
                "LEFT JOIN modelo m ON v.modelo_id = m.id " +
                "LEFT JOIN chofer c ON vp.chofer_id = c.id " +
                "LEFT JOIN vehiculo_tipo_cargas vtc ON v.id = vtc.vehiculo_id " +
                "LEFT JOIN remolque_tipo_cargas rtc ON r.id = rtc.remolque_id " +
                "LEFT JOIN viaje_personal_coincidencias vpc ON v.id = vpc.viaje_personal_id " +
                "LEFT JOIN coincidencia co ON vpc.coincidencias_id = co.id " +
                "LEFT JOIN pedido p ON co.pedido_id = p.id " +
                "LEFT JOIN ubicacion ui ON p.ubicacion_desde_id = ui.id " +
                "LEFT JOIN ubicacion uf ON p.ubicacion_hasta_id = uf.id " +
                "WHERE " + where(busqueda, transportador, igualODistinto) + " " +
                "GROUP BY vp.id ORDER BY " + order;

        Query query = em.createNativeQuery(queryString, ViajePersonal.class);

        List<ViajePersonal> viajes;
        if (pageable.isPaged()) {
            //noinspection unchecked
            viajes = query.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        } else {
            //noinspection unchecked
            viajes = query.getResultList();
        }

        Query countQuery = em.createNativeQuery("SELECT COUNT(*) FROM (" + queryString + ") AS count");

        long count = ((BigInteger) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(viajes, pageable, count);
    }

    private String where(BusquedaViajeModel busqueda, Transportador transportador, String igualODistinto) {
        StringBuilder where = new StringBuilder("vp.eliminado IS NULL AND ");

        if (!busqueda.getTipoDeRemolque().isEmpty()) {
            where.append("r.tipo_remolque_id='").append(busqueda.getTipoDeRemolque()).append(AND);
        }

        if (!busqueda.getTipoDeCamion().isEmpty()) {
            where.append("v.tipo_vehiculo_id='").append(busqueda.getTipoDeCamion()).append(AND);
        }

        if (!busqueda.getIdModelo().isEmpty()) {
            where.append("v.modelo_id='").append(busqueda.getIdModelo()).append(AND);
        }

        if (!busqueda.getChofer().isEmpty()) {
            String nombreOApellidoChofer = "%" + busqueda.getChofer() + "%";
            where.append("c.nombre LIKE '").append(nombreOApellidoChofer).append(AND);
        }

        if (!busqueda.getOrigen().isEmpty()) {
            String direccion = "%" + busqueda.getOrigen() + "%";
            where.append("ui.direccion LIKE '").append(direccion).append(AND);
        }

        if (!busqueda.getDestino().isEmpty()) {
            String direccion = "%" + busqueda.getDestino() + "%";
            where.append("ui.direccion LIKE '").append(direccion).append(AND);
        }

        if (!busqueda.getFechaDesde().isEmpty()) {
            where.append("vp.fecha_inicio LIKE '").append(busqueda.getFechaDesde()).append("%' AND ");
        }

        if (!busqueda.getFechaHasta().isEmpty()) {
            where.append("vp.fecha_final LIKE '").append(busqueda.getFechaHasta()).append("%' AND ");
        }

        if (busqueda.getKms() != null && busqueda.getKms() > 0) {
            where.append("vp.kms=").append(busqueda.getKms()).append(" AND ");
        }

        if (!busqueda.getTipoDeCarga().isEmpty()) {
            String tiposDeCarga = StringUtils.collectionToDelimitedString(busqueda.getTipoDeCarga(), ",", "'", "'");
            where.append("(vtc.tipo_cargas_id IN (").append(tiposDeCarga).append(") OR ")
                    .append("rtc.tipo_cargas_id IN (").append(tiposDeCarga).append(")) AND ");
        }

        if (!busqueda.getVehiculo().isEmpty()) {
            where.append("v.id='").append(busqueda.getVehiculo()).append(AND);
        }

        if (!busqueda.getQ().isEmpty()) {
            String otro = "%" + busqueda.getQ() + "%";
            where.append("(t.nombre LIKE '").append(otro).append("' OR ")
                    .append("c.nombre LIKE '").append(otro).append("' OR ")
                    .append("c.apellido LIKE '").append(otro).append("') AND ");
        }

        if (transportador != null) {
            where.append("vp.transportador_id").append(igualODistinto).append(transportador.getId()).append(AND);
        }

        where.delete(where.length() - 5, where.length());
        return where.toString();
    }

}
