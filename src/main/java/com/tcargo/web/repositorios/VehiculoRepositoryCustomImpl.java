package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.modelos.busqueda.BusquedaVehiculoModel;
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
public class VehiculoRepositoryCustomImpl implements VehiculoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String AND = "' AND ";

    @Override
    public Page<Vehiculo> buscarPorCriterios(Pageable pageable, BusquedaVehiculoModel busqueda, String idTransportador) {
        String order = "ma.nombre, m.nombre";

        if (pageable.getSort().isSorted()) {
            order = StringUtils.collectionToCommaDelimitedString(
                    StreamSupport.stream(pageable.getSort().spliterator(), false)
                            .map(o -> o.getProperty() + " " + o.getDirection())
                            .collect(Collectors.toList())
            );
        }

        String queryString = "SELECT v.* FROM vehiculo v " +
                "LEFT JOIN vehiculo_tipo_cargas vtc on v.id = vtc.vehiculo_id " +
                "LEFT JOIN modelo m ON v.modelo_id = m.id " +
                "LEFT JOIN marca ma ON m.marca_id = ma.id " +
                "LEFT JOIN transportador t ON v.transportador_id = t.id " +
                "LEFT JOIN tipo_vehiculo tv ON v.tipo_vehiculo_id = tv.id " +
                "WHERE " + where(busqueda, idTransportador) + " " +
                "GROUP BY v.id ORDER BY " + order;

        Query query = em.createNativeQuery(queryString, Vehiculo.class);

        List<Vehiculo> vehiculos;
        if (!pageable.isPaged()) {
            //noinspection unchecked
            vehiculos = query.getResultList();
        } else {
            //noinspection unchecked
            vehiculos = query.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        }

        Query countQuery = em.createNativeQuery("SELECT COUNT(*) FROM (" + queryString + ") AS count");

        long count = ((BigInteger) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(vehiculos, pageable, count);
    }

    private String where(
            BusquedaVehiculoModel busqueda, String idTransportador) {
        StringBuilder where = new StringBuilder("v.eliminado IS NULL AND ");
        if (idTransportador != null && !idTransportador.isEmpty()) {
            where.append("v.transportador_id='").append(idTransportador).append(AND);
        }

        if (!busqueda.getMarca().isEmpty()) {
            where.append("m.marca_id='").append(busqueda.getMarca()).append(AND);
        }

        if (!busqueda.getModelo().isEmpty()) {
            where.append("m.id='").append(busqueda.getModelo()).append(AND);
        }

        if (!busqueda.getTipoVehiculo().isEmpty()) {
            where.append("tv.nombre='").append(busqueda.getTipoVehiculo()).append(AND);
        }

        if (!busqueda.getAnio().isEmpty()) {
            where.append("v.anio_fabricacion='").append(busqueda.getAnio()).append(AND);
        }

        if (!busqueda.getQuery().isEmpty()) {
            String query = "%" + busqueda.getQuery() + "%";
            where.append("(v.dominio LIKE '").append(query).append("' OR ")
                    .append("m.nombre LIKE '").append(query).append("' OR ")
                    .append("ma.nombre LIKE '").append(query).append("') AND ");
        }

        if (busqueda.isTieneRastreo()) {
            where.append("v.rastreo_satelital=true AND ");
        }

        if (busqueda.isTieneSensores()) {
            where.append("v.sensores=true AND ");
        }

        if (!busqueda.getIdsTipoCarga().isEmpty()) {
            String tiposDeCarga = StringUtils.collectionToDelimitedString(busqueda.getIdsTipoCarga(), ",", "'", "'");
            where.append("vtc.tipo_cargas_id IN (").append(tiposDeCarga).append(") AND ");
        }

        where.delete(where.length() - 5, where.length());
        return where.toString();
    }

}
