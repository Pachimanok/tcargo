package com.tcargo.web;

import com.tcargo.web.lucene.LuceneIndexConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(LuceneIndexConfig.class)
@EnableScheduling
public class TcargoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcargoApplication.class, args);
    }

}
