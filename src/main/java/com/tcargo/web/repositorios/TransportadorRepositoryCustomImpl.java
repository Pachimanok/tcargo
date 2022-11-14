package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.modelos.busqueda.BusquedaTransportadorModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class TransportadorRepositoryCustomImpl implements TransportadorRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Transportador> buscarFiltrados(Pageable pageable, BusquedaTransportadorModel busqueda, boolean excel) {
        String queryString = "SELECT t FROM Transportador t WHERE t.eliminado IS NULL AND " +
                "t.usuario.ubicacion.direccion LIKE '%:domicilio%' AND t.usuario.mail LIKE '%:email%' AND " +
                "t.nombre LIKE '%:nombre%' AND t.usuario.telefono LIKE '%:telefono%' ORDER BY :order";

        String order = StringUtils.collectionToCommaDelimitedString(
                StreamSupport.stream(pageable.getSort().spliterator(), false)
                        .map(o -> o.getProperty() + " " + o.getDirection())
                        .collect(Collectors.toList())
        );

        queryString = queryString.replace(":domicilio", busqueda.getDomicilio())
                .replace(":email", busqueda.getEmail())
                .replace(":nombre", busqueda.getNombre())
                .replace(":telefono", busqueda.getTelefono())
                .replace(":order", order);

        if (busqueda.getIdPais() != null) {
            queryString = queryString + " AND t.usuario.pais.id = '" + busqueda.getIdPais() + "'";
        }

        Query query = em.createQuery(queryString, Transportador.class);

        List<Transportador> transportadores;
        if (!excel) {
            //noinspection unchecked
            transportadores = query.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        } else {
            //noinspection unchecked
            transportadores = query.getResultList();
        }

        queryString = queryString.replaceFirst("t", "COUNT(t)");
        Query countQuery = em.createQuery(queryString);

        long count = (long) countQuery.getSingleResult();

        return new PageImpl<>(transportadores, pageable, count);
    }
}
