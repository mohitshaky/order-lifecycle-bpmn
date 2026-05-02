package com.mohit.om.service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConsumerConfig class - Kafka consumer factory configuration for order management service
 *
 * @author mohit
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates a ConsumerFactory for OrderStatusEvent messages
     *
     * @return ConsumerFactory configured for JSON deserialization
     */
    @Bean
    public ConsumerFactory<String, Object> orderStatusConsumerFactory() {
        LOG.info("KafkaConsumerConfig:: orderStatusConsumerFactory method started");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.mohit.om.service.model");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.mohit.om.service.model.OrderStatusEvent");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a ConcurrentKafkaListenerContainerFactory for order status topic
     *
     * @return ConcurrentKafkaListenerContainerFactory
     */
    @Bean(name = "orderStatusListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> orderStatusListenerFactory() {
        LOG.info("KafkaConsumerConfig:: orderStatusListenerFactory method started");
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderStatusConsumerFactory());
        return factory;
    }

    /**
     * Creates a ConsumerFactory for ProvisioningEvent messages
     *
     * @return ConsumerFactory configured for JSON deserialization
     */
    @Bean
    public ConsumerFactory<String, Object> provisioningConsumerFactory() {
        LOG.info("KafkaConsumerConfig:: provisioningConsumerFactory method started");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.mohit.om.service.model");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.mohit.om.service.model.ProvisioningEvent");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a ConcurrentKafkaListenerContainerFactory for provisioning status topic
     *
     * @return ConcurrentKafkaListenerContainerFactory
     */
    @Bean(name = "provisioningListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> provisioningListenerFactory() {
        LOG.info("KafkaConsumerConfig:: provisioningListenerFactory method started");
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(provisioningConsumerFactory());
        return factory;
    }
}
