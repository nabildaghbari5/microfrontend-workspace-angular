package com.talentcloud.notification_ms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.talentcloud.notification_ms.client.UserClient;
import com.talentcloud.notification_ms.dto.*;
import com.talentcloud.notification_ms.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileStatusChangedEvent;
import com.talentcloud.notification_ms.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EmailService emailService;
    private final UserClient userClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @KafkaListener(topics = "application-submitted-topic", groupId = "notification-consumer-group")
    public void consumeApplicationSubmitted(ConsumerRecord<String, String> record) throws Exception {
        ApplicationSubmittedEvent event = objectMapper.readValue(record.value(), ApplicationSubmittedEvent.class);
        log.info("📨 ApplicationSubmittedEvent received: {}", event);

        if (event.getClientEmail() != null && !event.getClientEmail().isEmpty()) {
            String clientUserId = null;
            try {
                log.info("📧 Sending application notification to client: {}", event.getClientEmail());
            } catch (Exception e) {
                log.warn("⚠️ Could not get client userId, proceeding with email only");
            }

            // Get job title with null handling
            String jobTitle = (event.getJobTitle() != null && !event.getJobTitle().trim().isEmpty())
                    ? event.getJobTitle()
                    : "your job";

            // Updated message format
            String message = String.format("There is someone that applied for your job: %s", jobTitle);

            emailService.sendEmail(
                    clientUserId,
                    event.getClientEmail(),
                    "📥 New Application Submitted",
                    message
            );

            log.info("✅ Successfully sent notification email to client: {}", event.getClientEmail());
        } else {
            log.warn("⚠️ Client email is missing in ApplicationSubmittedEvent: {}", event);
        }
    }

    @KafkaListener(topics = "application-status-changed-topic", groupId = "notification-consumer-group")
    public void consumeStatusChanged(ConsumerRecord<String, String> record) throws Exception {
        ApplicationStatusChangedEvent event = objectMapper.readValue(record.value(), ApplicationStatusChangedEvent.class);
        log.info("⚠️ ApplicationStatusChangedEvent received: {}", event);

        String candidateEmail = event.getCandidateEmail();
        String candidateUserId = event.getCandidateId();

        // Try to get email if missing
        if (candidateEmail == null && candidateUserId != null) {
            try {
                Map<String, String> response = userClient.getEmailByUserId(candidateUserId);
                candidateEmail = response.get("email");
                log.info("📧 Retrieved candidate email from auth-ms: {}", candidateEmail);
            } catch (Exception e) {
                log.error("❌ Failed to fetch candidate email from auth-ms for userId: {}", candidateUserId, e);
            }
        }

        if (candidateEmail != null) {
            String subject;
            String message;

            // Customize subject and message based on the new status
            if ("ACCEPTED".equalsIgnoreCase(event.getNewStatus())) {
                subject = "🎉 Congratulations - Application Accepted!";
                message = "Congratulations! Your application for '" + event.getJobTitle() + "' has been accepted. We look forward to having you on our team!";
            } else if ("REFUSED".equalsIgnoreCase(event.getNewStatus())) {
                subject = "📝 Application Update";
                message = "Unfortunately, your application for '" + event.getJobTitle() + "' has been refused. We hope you find another job opportunity that suits you well.";
            } else {
                // Default message for other status changes
                subject = "📌 Application Status Changed";
                message = "Your application for '" + event.getJobTitle() +
                        "' has been updated from '" + event.getOldStatus() +
                        "' to '" + event.getNewStatus() + "'.";
            }

            emailService.sendEmail(
                    candidateUserId, // Can be null
                    candidateEmail,
                    subject,
                    message
            );
        } else {
            log.warn("⚠️ Candidate email is missing for application ID: {}", event.getApplicationId());
        }
    }

    @KafkaListener(topics = "job-created-topic", groupId = "notification-consumer-group")
    public void consumeJobCreated(ConsumerRecord<String, String> record) throws Exception {
        JobOfferCreatedEvent event = objectMapper.readValue(record.value(), JobOfferCreatedEvent.class);
        log.info("🆕 JobOfferCreatedEvent received: {}", event);

        try {
            Map<String, String> response = userClient.getEmailByUserId(event.getClientId());
            String clientEmail = response.get("email");

            if (clientEmail != null) {
                emailService.sendEmail(
                        event.getClientId(), // We have the userId
                        clientEmail,
                        "✅ Job Offer Created",
                        "Your job offer '" + event.getJobTitle() + "' has been successfully posted at " +
                                event.getTimestamp() + "."
                );
            } else {
                log.warn("❌ No email found for user ID: {}", event.getClientId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch email from auth-ms for userId: {}", event.getClientId(), e);
        }
    }

    @KafkaListener(
            topics = "profile-created-topic",
            groupId = "notification-profile-created-group",
            containerFactory = "profileCreatedKafkaListenerContainerFactory"
    )
    public void handleProfileCreatedEvent(ProfileCreatedEvent event) {
        try {
            log.info("📥 Received ProfileCreatedEvent: {}", event);

            // Validate that we have the required email
            if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
                log.error("❌ ProfileCreatedEvent missing email, cannot process");
                return;
            }

            // Process the event - send welcome email
            emailService.sendWelcomeEmail(event);

            log.info("✅ Successfully processed ProfileCreatedEvent for user: {} (email: {})",
                    event.getUserId(), event.getEmail());

        } catch (Exception e) {
            log.error("❌ Error processing ProfileCreatedEvent for user {} (email: {}): {}",
                    event.getUserId(), event.getEmail(), e.getMessage(), e);
            // You might want to add retry logic or dead letter queue here
        }
    }

    @KafkaListener(
            topics = "candidate-profile-status-topic",
            groupId = "notification-candidate-status-group",
            containerFactory = "profileStatusChangedKafkaListenerContainerFactory"
    )
    public void handlecandidateProfileStatusChangedEvent(ProfileStatusChangedEvent event) {
        try {
            log.info("📥 Received ProfileStatusChangedEvent: {}", event);

            // Validate that we have the required email
            if (event.getUserEmail() == null || event.getUserEmail().trim().isEmpty()) {
                log.error("❌ ProfileStatusChangedEvent missing userEmail, cannot process");
                return;
            }

            // Process the event - send status notification email
            emailService.sendProfileStatusEmail(event);

            log.info("✅ Successfully processed ProfileStatusChangedEvent for user: {} (email: {})",
                    event.getUserId(), event.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Error processing ProfileStatusChangedEvent for user {} (email: {}): {}",
                    event.getUserId(), event.getUserEmail(), e.getMessage(), e);
            // You might want to add retry logic or dead letter queue here
        }
    }

    @KafkaListener(
            topics = "client-profile-created-topic",
            groupId = "notification-client-profile-group",
            containerFactory = "clientProfileCreatedKafkaListenerContainerFactory"
    )
    public void handleClientProfileCreatedEvent(ClientProfileCreatedEvent event) {
        try {
            log.info("📥 Received ClientProfileCreatedEvent: {}", event);

            // Validate that we have the required email
            if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
                log.error("❌ ClientProfileCreatedEvent missing email, cannot process");
                return;
            }

            // Send welcome email for client
            emailService.sendWelcomeEmailForClient(event);

            log.info("✅ Successfully processed ClientProfileCreatedEvent for clientId: {} (email: {})",
                    event.getClientId(), event.getEmail());

        } catch (Exception e) {
            log.error("❌ Error processing ClientProfileCreatedEvent for client {} (email: {}): {}",
                    event.getClientId(), event.getEmail(), e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "client-profile-status-topic",
            groupId = "notification-client-status-group",
            containerFactory = "profileStatusChangedKafkaListenerContainerFactory"
    )
    public void handleClientProfileStatusChanged(ProfileStatusChangedEvent event) {
        try {
            log.info("📥 Client status changed: {}", event);

            // Validate that we have the required email
            if (event.getUserEmail() == null || event.getUserEmail().trim().isEmpty()) {
                log.error("❌ Client ProfileStatusChangedEvent missing userEmail, cannot process");
                return;
            }

            emailService.sendProfileStatusEmail(event);

            log.info("✅ Successfully processed Client ProfileStatusChangedEvent for user: {} (email: {})",
                    event.getUserId(), event.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Error processing Client ProfileStatusChangedEvent for user {} (email: {}): {}",
                    event.getUserId(), event.getUserEmail(), e.getMessage(), e);
        }
    }
}