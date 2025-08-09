package com.talentcloud.notification_ms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobOfferCreatedEvent {
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
    private Long jobOfferId;
    private String clientId;
    private String jobTitle;
    private String jobDescription;
    private String location;
    private String employmentType;
}
