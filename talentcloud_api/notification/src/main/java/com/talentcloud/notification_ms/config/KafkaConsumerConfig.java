package com.talentcloud.notification_ms.config;

import com.talentcloud.notification_ms.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileStatusChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> getCommonConsumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.talentcloud.notification_ms.dto.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    // -------------------- ProfileCreatedEvent --------------------
    @Bean
    public ConsumerFactory<String, ProfileCreatedEvent> profileCreatedEventConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-profile-created-group");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProfileCreatedEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProfileCreatedEvent> profileCreatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProfileCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(profileCreatedEventConsumerFactory());
        factory.setCommonErrorHandler(getDefaultErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // -------------------- ProfileStatusChangedEvent --------------------
    @Bean
    public ConsumerFactory<String, ProfileStatusChangedEvent> profileStatusChangedEventConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-profile-status-group");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProfileStatusChangedEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProfileStatusChangedEvent> profileStatusChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProfileStatusChangedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(profileStatusChangedEventConsumerFactory());
        factory.setCommonErrorHandler(getDefaultErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // -------------------- ClientProfileCreatedEvent --------------------
    @Bean
    public ConsumerFactory<String, ClientProfileCreatedEvent> clientProfileConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-client-profile-group");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ClientProfileCreatedEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClientProfileCreatedEvent> clientProfileCreatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClientProfileCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clientProfileConsumerFactory());
        factory.setCommonErrorHandler(getDefaultErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // -------------------- ErrorHandler Commun --------------------
    private DefaultErrorHandler getDefaultErrorHandler() {
        return new DefaultErrorHandler(
                (record, ex) -> {
                    System.err.println("❌ Error processing record: " + record.value() + ", cause: " + ex.getMessage());
                },
                new FixedBackOff(1000L, 3)
        );
    }
}
