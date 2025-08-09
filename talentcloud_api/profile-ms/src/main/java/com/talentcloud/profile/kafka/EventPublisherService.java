// ✅ EventPublisherService.java
package com.talentcloud.profile.kafka;

import com.talentcloud.profile.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTemplate<String, ClientProfileCreatedEvent> clientCreatedKafkaTemplate;

    private static final String PROFILE_CREATED_TOPIC = "profile-created-topic";
    private static final String CLIENT_PROFILE_CREATED_TOPIC = "client-profile-created-topic";
    private static final String CANDIDATE_PROFILE_STATUS_TOPIC = "candidate-profile-status-topic";
    private static final String CLIENT_PROFILE_STATUS_TOPIC = "client-profile-status-topic";

    public void publishProfileCreatedEvent(ProfileCreatedEvent event) {
        try {
            kafkaTemplate.send(PROFILE_CREATED_TOPIC, event.getUserId(), event);
            log.info("✅ ProfileCreatedEvent published for user: {}, profileType: {}", event.getUserId(), event.getProfileType());
        } catch (Exception e) {
            log.error("❌ Failed to publish ProfileCreatedEvent for user: {}", event.getUserId(), e);
        }
    }

    public void publishProfileStatusChangedEvent(ProfileStatusChangedEvent event) {
        try {
            String topic = switch (event.getUserType()) {
                case "CLIENT" -> CLIENT_PROFILE_STATUS_TOPIC;
                case "CANDIDATE" -> CANDIDATE_PROFILE_STATUS_TOPIC;
                default -> throw new IllegalArgumentException("Invalid user type: " + event.getUserType());
            };
            kafkaTemplate.send(topic, event.getUserId(), event);
            log.info("✅ ProfileStatusChangedEvent published to {} for user: {}, status: {}", topic, event.getUserId(), event.getProfileStatus());
        } catch (Exception e) {
            log.error("❌ Failed to publish ProfileStatusChangedEvent for user: {}", event.getUserId(), e);
        }
    }

    public void publishClientProfileCreatedEvent(ClientProfileCreatedEvent event) {
        try {
            clientCreatedKafkaTemplate.send(CLIENT_PROFILE_CREATED_TOPIC, event.getUserId(), event);
            log.info("✅ ClientProfileCreatedEvent published for user: {}, clientId: {}", event.getUserId(), event.getClientId());
        } catch (Exception e) {
            log.error("❌ Failed to publish ClientProfileCreatedEvent for user: {}", event.getUserId(), e);
        }
    }
}
