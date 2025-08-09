package com.talentcloud.profile.kafka;

import com.talentcloud.profile.dto.event.ProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileEventProducer {

    private final KafkaTemplate<String, ProfileCreatedEvent> kafkaTemplate;
    private final KafkaTemplate<String, ProfileStatusChangedEvent> statusChangedKafkaTemplate;

    private static final String TOPIC = "profile-created-topic";
    private static final String CANDIDATE_STATUS_TOPIC = "candidate-profile-status-topic";
    private static final String CLIENT_STATUS_TOPIC = "client-profile-status-topic";

    public void sendProfileCreatedEvent(ProfileCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId(), event);
        log.info("📤 Sent ProfileCreatedEvent to Kafka: {}", event);
    }

    public void sendProfileStatusChangedEvent(ProfileStatusChangedEvent event) {
        String topic = switch (event.getUserType()) {
            case "CANDIDATE" -> CANDIDATE_STATUS_TOPIC;
            case "CLIENT" -> CLIENT_STATUS_TOPIC;
            default -> throw new IllegalArgumentException("Invalid user type: " + event.getUserType());
        };
        statusChangedKafkaTemplate.send(topic, event.getUserId(), event);
        log.info("📤 Sent ProfileStatusChangedEvent to topic {} for user {}: {}", topic, event.getUserId(), event);
    }
}
