package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.modelos.busqueda.BusquedaUsuarioModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UsuarioRepositoryCustomImpl implements UsuarioRepositoryCustom {

    private static final String MODIFICACION = "modificacion";
    private static final Log log = LogFactory.getLog(UsuarioRepositoryCustomImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Usuario> buscarPorCriterios(Pageable pageable, BusquedaUsuarioModel busqueda, boolean excel) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Usuario> query = builder.createQuery(Usuario.class);
        Root<Usuario> usuario = query.from(Usuario.class);

        query.where(builder.and(getPredicates(busqueda, usuario, builder).toArray(new Predicate[0])));

        List<Usuario> usuarios;

        if (excel) {
            usuarios = em.createQuery(query.orderBy(QueryUtils.toOrders(pageable.getSort(), usuario, builder)))
                    .getResultList();
        } else {
            usuarios = em.createQuery(query.orderBy(QueryUtils.toOrders(pageable.getSort(), usuario, builder)))
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Usuario> countRoot = countQuery.from(query.getResultType());
        countQuery.select(builder.count(countRoot)).where(builder.and(getPredicates(busqueda, countRoot, builder).toArray(new Predicate[0])));

        Long count = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(usuarios, pageable, count);
    }

    private List<Predicate> getPredicates(BusquedaUsuarioModel busqueda, Root<Usuario> usuario, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        if (busqueda.getRol() != null) {
            if (busqueda.getRol().equals("ADMINISTRADOR")) {
                Predicate p1 = builder.equal(usuario.get("rol"), Rol.ADMINISTRADOR);
                Predicate p2 = builder.equal(usuario.get("rol"), Rol.ADMINISTRADOR_LOCAL);
                predicates.add(builder.or(p1, p2));
            } else {
                try {
                    predicates.add(builder.equal(usuario.get("rol"), Rol.getRol(busqueda.getRol())));
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage());
                }
            }
        }

        if (busqueda.getOtro() != null && !busqueda.getOtro().isEmpty()) {
            String otro = "%" + busqueda.getOtro() + "%";
            Predicate nombre = builder.like(usuario.get("nombre"), otro);
            Predicate mail = builder.like(usuario.get("mail"), otro);
            Predicate cuit = builder.like(usuario.get("cuit"), otro);
            Predicate telefono = builder.like(usuario.get("telefono"), otro);
            predicates.add(builder.or(nombre, mail, cuit, telefono));
        }

        if (busqueda.getFechaInicio() != null && busqueda.getFechaFinalizacion() != null) {
            predicates.add(builder.between(usuario.get(MODIFICACION), convertirFechaInicio(busqueda.getFechaInicio()), convertirFecha(busqueda.getFechaFinalizacion())));
        } else if (busqueda.getFechaInicio() != null) {
            predicates.add(builder.lessThanOrEqualTo(usuario.get(MODIFICACION), convertirFecha(busqueda.getFechaInicio())));
        } else if (busqueda.getFechaFinalizacion() != null) {
            predicates.add(builder.lessThanOrEqualTo(usuario.get(MODIFICACION), convertirFecha(busqueda.getFechaFinalizacion())));
        }

        if (busqueda.getIdPais() != null && !busqueda.getIdPais().isEmpty()) {
            Join<Usuario, Pais> pais = usuario.join("pais", JoinType.LEFT);
            predicates.add(builder.like(pais.get("id"), busqueda.getIdPais()));
        }

        predicates.add(builder.notLike(usuario.get("id"), busqueda.getIdUsuario()));
        predicates.add(builder.isNull(usuario.get("eliminado")));

        return predicates;
    }

    private Date convertirFecha(LocalDate fecha) {
        return Date.from(fecha.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date convertirFechaInicio(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
