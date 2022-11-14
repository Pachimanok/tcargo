package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Ubicacion;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class CustomizedUbicacionRepositoryImpl implements CustomizedUbicacionRepository {

    private final EntityManager entityManager;

    @Autowired
    public CustomizedUbicacionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Ubicacion> buscarCerca(Ubicacion ubicacion, Double radio) {
        double distanciaBusqueda = radio != null ? radio : 100D;
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder builder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Ubicacion.class)
                .get();

        Query luceneQuery = builder.spatial()
                .within(distanciaBusqueda, Unit.KM)
                .ofLatitude(ubicacion.getLatitud())
                .andLongitude(ubicacion.getLongitud())
                .createQuery();

        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Ubicacion.class);

        @SuppressWarnings("unchecked")
        List<Ubicacion> ubicacionesCercanas = fullTextQuery.getResultList();

        return ubicacionesCercanas;
    }

}
