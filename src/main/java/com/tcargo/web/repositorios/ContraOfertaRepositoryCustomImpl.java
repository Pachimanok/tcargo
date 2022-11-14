package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.modelos.busqueda.BusquedaHistorialModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ContraOfertaRepositoryCustomImpl implements ContraOfertaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String OR = "' OR ";

    @Override
    public Page<ContraOferta> buscarPorCriterios(Pageable pageable, Long idPedido, Double valor, String vehiculo, String otro, boolean excel) {
        String order;
        if (pageable.getSort().isSorted()) {
            order = StringUtils.collectionToCommaDelimitedString(
                    StreamSupport.stream(pageable.getSort().spliterator(), false)
                            .map(o -> o.getProperty() + " " + o.getDirection())
                            .collect(Collectors.toList())
            );
        } else {
            order = "c.valor asc";
        }

        final String from = "ContraOferta c";
        final String where = where(idPedido, valor, vehiculo, otro);
        final String queryString = "SELECT c FROM ContraOferta c WHERE " + where +
                " ORDER BY " + order;

        Query query = entityManager.createQuery(queryString, ContraOferta.class);

        List<ContraOferta> contraOfertas;
        if (excel) {
            //noinspection unchecked
            contraOfertas = query.getResultList();
        } else {
            //noinspection unchecked
            contraOfertas = query.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        }

        Query countQuery = entityManager.createQuery("SELECT COUNT(c) FROM " + from + " WHERE " + where);

        long count = (long) countQuery.getSingleResult();

        return new PageImpl<>(contraOfertas, pageable, count);
    }

    private String where(Long idPedido, Double valor, String vehiculo, String otro) {
        StringBuilder where = new StringBuilder("c.eliminado IS NULL AND ");

        if (idPedido != null) {
            where.append("c.pedido.id=").append(idPedido).append(" AND ");
        }

        if (valor != null) {
            where.append("c.valor=").append(valor).append(" AND ");
        }
        if (vehiculo != null && !vehiculo.isEmpty()) {
            where.append("c.vehiculo.modelo.nombre LIKE '%").append(vehiculo).append("%' AND ");
        }

        if (otro != null && !otro.isEmpty()) {
            String otroLike = "%" + otro + "%";
            where.append("(c.transportador.usuario.nombre LIKE '").append(otroLike).append(OR)
                    .append("c.transportador.nombre LIKE '").append(otroLike).append(OR)
                    .append("c.dador.nombre LIKE '").append(otroLike).append(OR)
                    .append("c.pedido.kmTotales LIKE '").append(otroLike).append(OR)
                    .append("c.pedido.ubicacionDesde.direccion LIKE '").append(otroLike).append(OR)
                    .append("c.pedido.ubicacionHasta.direccion LIKE '").append(otroLike).append(OR)
                    .append("c.estado LIKE '").append(otroLike).append("') AND ");
        }
        where.delete(where.length() - 5, where.length());
        return where.toString();
    }

    @Override
    public Page<ContraOferta> buscarPorTransportador(Pageable pageable, String idTransportador, BusquedaHistorialModel busqueda) {
        return this.<Transportador>buscar(pageable, "transportador", idTransportador, busqueda);
    }

    @Override
    public Page<ContraOferta> buscarPorDador(Pageable pageable, String idDador, BusquedaHistorialModel busqueda) {
        return this.<Usuario>buscar(pageable, "dador", idDador, busqueda);
    }

    private <T> Page<ContraOferta> buscar(Pageable pageable, String dadorOTransportador, String id, BusquedaHistorialModel busqueda) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ContraOferta> query = builder.createQuery(ContraOferta.class);
        Root<ContraOferta> contraOferta = query.from(ContraOferta.class);

        // Arma la query con todos los predicados que estén en la lista.
        query.where(builder.and(this.<T>getPredicates(builder, contraOferta, busqueda, dadorOTransportador, id).toArray(new Predicate[0])));

        // Devuelve la página que corresponde.
        List<ContraOferta> result = entityManager.createQuery(query.orderBy(QueryUtils.toOrders(pageable.getSort(), contraOferta, builder)))
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Crea la query para contar la cantidad total de elementos que hay según los parámetros de búsqueda.
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<ContraOferta> root = countQuery.from(query.getResultType());
        countQuery.select(builder.count(root))
                .where(builder.and(this.<T>getPredicates(builder, root, busqueda, dadorOTransportador, id)
                        .toArray(new Predicate[0])));

        // Devuelve la cantidad total de resultados
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, count);
    }

    private <T> List<Predicate> getPredicates(CriteriaBuilder builder, Root<ContraOferta> root, BusquedaHistorialModel busqueda,
                                              String dadorOTransportador, String id) {
        List<Predicate> predicates = new ArrayList<>();

        // Estos if verifican que los filtros tengan algún valor. Si tienen, los añade a la lista de predicados.
        if (busqueda.getEstado() != null) {
            predicates.add(builder.equal(root.get("estado"), busqueda.getEstado()));
        }
        if (busqueda.getValor() != null) {
            predicates.add(builder.le(root.get("valor"), busqueda.getValor()));
        }
        if (!busqueda.getComentario().isEmpty()) {
            predicates.add(builder.like(root.get("comentarios"), "%".concat(busqueda.getComentario()).concat("%")));
        }
        if (!busqueda.getModificacion().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                LocalDate diaSiguiente = LocalDate.parse(busqueda.getModificacion(), dtf).plusDays(1);
                predicates.add(builder.and(builder.greaterThanOrEqualTo(root.get("modificacion"), sdf.parse(busqueda.getModificacion())), builder.lessThan(root.get("modificacion"), Date.from(diaSiguiente.atStartOfDay(ZoneId.systemDefault()).toInstant()))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (busqueda.getIdPedido() != null) {
            Join<ContraOferta, Pedido> join = root.join("pedido", JoinType.INNER);
            predicates.add(builder.equal(join.get("id"), busqueda.getIdPedido()));
        }

        // Busca por dador o transportador.
        Join<ContraOferta, T> join = root.join(dadorOTransportador, JoinType.INNER);
        predicates.add(builder.equal(join.get("id"), id));

        return predicates;
    }

}
