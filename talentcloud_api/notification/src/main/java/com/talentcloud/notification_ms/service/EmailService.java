package com.talentcloud.notification_ms.service;

import com.talentcloud.notification_ms.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileCreatedEvent;
import com.talentcloud.notification_ms.dto.event.ProfileStatusChangedEvent;
import com.talentcloud.notification_ms.model.Notification;
import com.talentcloud.notification_ms.model.NotificationState;
import com.talentcloud.notification_ms.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public void sendEmail(String to, String subject, String body) {
        sendEmail(null, to, subject, body);
    }

    public void sendEmail(String userId, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@talentcloud.com");

            mailSender.send(message);
            log.info("📧 Email sent to {}", to);

            // 🔔 Save notification with improved logic
            saveNotification(userId, to, subject, body);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}", to, e);
        }
    }

    public void sendWelcomeEmail(ProfileCreatedEvent event) {
        try {
            log.info("🔄 Preparing welcome email for: {}", event.getEmail());

            String subject = "Welcome to TalentCloud! 🎉";
            String emailBody = buildWelcomeEmailBody(event);

            sendEmail(event.getUserId(), event.getEmail(), subject, emailBody);

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", event.getEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendProfileStatusEmail(ProfileStatusChangedEvent event) {
        try {
            log.info("🔄 Preparing status email for: {}", event.getUserEmail());

            String subject;
            if ("APPROVED".equals(event.getProfileStatus())) {
                subject = "🎉 Your Profile Has Been Approved!";
            } else if ("REJECTED".equals(event.getProfileStatus())) {
                subject = "❌ Profile Update Required";
            } else {
                subject = "Profile Status Update";
            }

            String body = buildStatusEmailBody(event);

            sendEmail(event.getUserId(), event.getUserEmail(), subject, body);

        } catch (Exception e) {
            log.error("❌ Failed to send status email to {}: {}", event.getUserEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendWelcomeEmailForClient(ClientProfileCreatedEvent event) {
        try {
            log.info("🔄 Preparing welcome email for client: {}", event.getEmail());

            String subject = "🎉 Welcome to TalentCloud as a Client!";
            StringBuilder body = new StringBuilder();

            body.append("Hello");
            if (event.getFirstName() != null) {
                body.append(" ").append(event.getFirstName());
            }
            body.append("!\n\n");
            body.append("Welcome to TalentCloud 🎉\n\n");
            body.append("Your client profile has been successfully created and is now under review.\n");
            body.append("Status: ").append(event.getStatus()).append("\n\n");
            body.append("You'll receive another email once your profile has been approved.\n\n");
            body.append("Best regards,\nThe TalentCloud Team");

            sendEmail(event.getUserId(), event.getEmail(), subject, body.toString());

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to client {}: {}", event.getEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String buildWelcomeEmailBody(ProfileCreatedEvent event) {
        StringBuilder body = new StringBuilder();
        body.append("Hello");

        if (event.getFirstname() != null && !event.getFirstname().trim().isEmpty()) {
            body.append(" ").append(event.getFirstname());
        }

        body.append("!\n\n");
        body.append("Welcome to TalentCloud! 🎉\n\n");
        body.append("Thank you for creating your ").append(event.getProfileType() != null ? event.getProfileType().toLowerCase() : "").append(" profile.\n");
        body.append("Your profile is currently under review and has a status of: ").append(event.getStatus() != null ? event.getStatus() : "PENDING").append("\n\n");

        if ("CANDIDATE".equals(event.getProfileType()) && event.getJobTitle() != null) {
            body.append("We see you're interested in ").append(event.getJobTitle()).append(" positions. ");
            body.append("We'll notify you when relevant opportunities become available!\n\n");
        }

        body.append("You'll receive another email once your profile has been reviewed.\n\n");
        body.append("Best regards,\n");
        body.append("The TalentCloud Team");

        return body.toString();
    }

    private String buildStatusEmailBody(ProfileStatusChangedEvent event) {
        StringBuilder body = new StringBuilder();
        body.append("Hello!\n\n");

        if ("APPROVED".equals(event.getProfileStatus())) {
            body.append("Great news! 🎉\n\n");
            body.append("Your ").append(event.getUserType() != null ? event.getUserType().toLowerCase() : "").append(" profile has been approved!\n");
            body.append("You can now fully access all TalentCloud features.\n\n");

            if ("CANDIDATE".equals(event.getUserType())) {
                body.append("You can now:\n");
                body.append("- Apply for job positions\n");
                body.append("- Connect with employers\n");
                body.append("- Update your profile anytime\n\n");
            }

        } else if ("REJECTED".equals(event.getProfileStatus())) {
            body.append("Profile Review Update ❌\n\n");
            body.append("Unfortunately, your profile needs some updates before it can be approved.\n\n");
        }

        if (event.getMessage() != null && !event.getMessage().trim().isEmpty()) {
            body.append("Message: ").append(event.getMessage()).append("\n\n");
        }

        body.append("If you have any questions, please don't hesitate to contact our support team.\n\n");
        body.append("Best regards,\n");
        body.append("The TalentCloud Team");

        return body.toString();
    }

    /**
     * Enhanced saveNotification method with better handling of userId and email
     */
    private void saveNotification(String userId, String email, String title, String message) {
        try {
            // Validate that we have at least email
            if (email == null || email.trim().isEmpty()) {
                log.warn("⚠️ Cannot save notification: email is missing");
                return;
            }

            // Log what we're saving
            log.info("💾 Saving notification - UserId: {}, Email: {}, Title: {}",
                    userId != null ? userId : "null", email, title);

            Notification notification = Notification.builder()
                    .userId(userId) // Can be null
                    .email(email.trim().toLowerCase()) // Always present and normalized
                    .title(title)
                    .message(message)
                    .state(NotificationState.UNREAD)
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("✅ Notification saved with ID: {}", saved.getId());

        } catch (Exception e) {
            log.error("❌ Failed to save notification for email {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Method to update existing notifications with missing userIds
     * This can be called manually or scheduled to clean up old data
     */
    public void updateNotificationsWithMissingUserIds() {
        try {
            log.info("🔄 Starting cleanup of notifications without userId...");

            // This is a manual cleanup method - you would need to implement
            // the logic to map emails to userIds if needed
            var notificationsWithoutUserId = notificationRepository.findByUserIdIsNull();

            log.info("📊 Found {} notifications without userId", notificationsWithoutUserId.size());

            // For now, just log them - you could implement user lookup logic here
            notificationsWithoutUserId.forEach(notification -> {
                log.info("📧 Notification ID {} has email {} but no userId",
                        notification.getId(), notification.getEmail());
            });

        } catch (Exception e) {
            log.error("❌ Error during notification cleanup: {}", e.getMessage(), e);
        }
    }
}