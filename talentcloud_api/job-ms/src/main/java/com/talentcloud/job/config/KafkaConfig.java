package com.talentcloud.job.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.job-created}")
    private String jobCreatedTopic;

    @Value("${kafka.topics.application-submitted}")
    private String applicationSubmittedTopic;

    @Value("${kafka.topics.application-status-changed}")
    private String applicationStatusChangedTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic jobCreatedTopic() {
        return new NewTopic(jobCreatedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic applicationSubmittedTopic() {
        return new NewTopic(applicationSubmittedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic applicationStatusChangedTopic() {
        return new NewTopic(applicationStatusChangedTopic, 3, (short) 1);
    }
}