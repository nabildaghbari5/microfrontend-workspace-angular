package com.talentcloud.profile.config;

import com.talentcloud.profile.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileStatusChangedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    @Bean
    public ProducerFactory<String, ProfileCreatedEvent> profileCreatedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultConfigs());
    }

    @Bean
    public KafkaTemplate<String, ProfileCreatedEvent> profileCreatedKafkaTemplate() {
        return new KafkaTemplate<>(profileCreatedEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ProfileStatusChangedEvent> profileStatusChangedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultConfigs());
    }

    @Bean
    public KafkaTemplate<String, ProfileStatusChangedEvent> profileStatusChangedKafkaTemplate() {
        return new KafkaTemplate<>(profileStatusChangedEventProducerFactory());
    }

    private Map<String, Object> defaultConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // use env/config
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configuration optionnelle pour une meilleure performance
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    @Bean
    public ProducerFactory<String, ClientProfileCreatedEvent> clientProfileProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // modifie si nécessaire
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ClientProfileCreatedEvent> clientCreatedKafkaTemplate(
            ProducerFactory<String, ClientProfileCreatedEvent> pf) {
        return new KafkaTemplate<>(pf);
    }


}
