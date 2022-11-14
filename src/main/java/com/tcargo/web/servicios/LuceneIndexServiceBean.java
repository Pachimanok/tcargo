package com.tcargo.web.servicios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import javax.persistence.EntityManagerFactory;

public class LuceneIndexServiceBean {

    private final FullTextEntityManager fullTextEntityManager;
    private final Log log = LogFactory.getLog(LuceneIndexServiceBean.class);

    public LuceneIndexServiceBean(EntityManagerFactory entityManagerFactory) {
        fullTextEntityManager = Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
    }

    public void triggerIndexing() throws InterruptedException {
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            log.error("Error al indexar las ubicaciones." + e.getMessage());
            throw e;
        }
    }

}
