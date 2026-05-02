package com.mohit.om.service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaProducerConfig class - Kafka producer factory and KafkaTemplate configuration
 *
 * @author mohit
 */
@Configuration
public class KafkaProducerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates a ProducerFactory for publishing JSON messages
     *
     * @return ProducerFactory configured with JSON serializer
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        LOG.info("KafkaProducerConfig:: producerFactory method started");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate using the configured ProducerFactory
     *
     * @return KafkaTemplate for publishing messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        LOG.info("KafkaProducerConfig:: kafkaTemplate method started");
        return new KafkaTemplate<>(producerFactory());
    }
}
