package com.tcargo.web.lucene;

import com.tcargo.web.servicios.LuceneIndexServiceBean;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManagerFactory;

public class LuceneIndexConfig {

    @Bean
    public LuceneIndexServiceBean luceneIndexServiceBean(EntityManagerFactory entityManagerFactory) throws InterruptedException {
        LuceneIndexServiceBean luceneIndexServiceBean = new LuceneIndexServiceBean(entityManagerFactory);
        luceneIndexServiceBean.triggerIndexing();
        return luceneIndexServiceBean;
    }

}
