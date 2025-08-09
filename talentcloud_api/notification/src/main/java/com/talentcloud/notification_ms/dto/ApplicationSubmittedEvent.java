package com.talentcloud.notification_ms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationSubmittedEvent {
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
    private Long applicationId;
    private Long jobOfferId;
    private String candidateId;
    private String candidateName;
    private String candidateEmail;
    private String clientId;
    private String clientEmail;
    private String jobTitle;
}
