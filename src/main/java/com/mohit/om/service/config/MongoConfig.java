package com.mohit.om.service.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoConfig class - MongoDB MongoTemplate bean configuration
 *
 * @author mohit
 */
@Configuration
public class MongoConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * Creates the primary MongoClient bean
     *
     * @return MongoClient instance
     */
    @Bean
    @Primary
    public MongoClient mongoClient() {
        LOG.info("MongoConfig:: mongoClient method started");
        return MongoClients.create(mongoUri);
    }

    /**
     * Creates the primary MongoTemplate bean using the configured MongoClient
     *
     * @return MongoTemplate instance configured for orderdb
     */
    @Bean(name = "primaryMongoTemplate")
    @Primary
    public MongoTemplate primaryMongoTemplate() {
        LOG.info("MongoConfig:: primaryMongoTemplate method started");
        return new MongoTemplate(mongoClient(), "orderdb");
    }
}
