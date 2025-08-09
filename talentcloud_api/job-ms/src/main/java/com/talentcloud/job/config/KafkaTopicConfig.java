//package com.talentcloud.job.config;
//
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.KafkaAdmin;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaTopicConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    // Define topic names as constants
//    public static final String APPLICATION_SUBMITTED_TOPIC = "application-submitted-events";
//    public static final String APPLICATION_STATUS_CHANGED_TOPIC = "application-status-changed-events";
//    public static final String JOB_OFFER_CREATED_TOPIC = "job-offer-created-events";
//
//    // Topic configuration
//    private static final int NUM_PARTITIONS = 3;
//    private static final short REPLICATION_FACTOR = 1; // Use 3 for production with multiple brokers
//
//    @Bean
//    public KafkaAdmin kafkaAdmin() {
//        Map<String, Object> configs = new HashMap<>();
//        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        return new KafkaAdmin(configs);
//    }
//
//    @Bean
//    public NewTopic applicationSubmittedTopic() {
//        return new NewTopic(APPLICATION_SUBMITTED_TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR);
//    }
//
//    @Bean
//    public NewTopic applicationStatusChangedTopic() {
//        return new NewTopic(APPLICATION_STATUS_CHANGED_TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR);
//    }
//
//    @Bean
//    public NewTopic jobOfferCreatedTopic() {
//        return new NewTopic(JOB_OFFER_CREATED_TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR);
//    }
//}
