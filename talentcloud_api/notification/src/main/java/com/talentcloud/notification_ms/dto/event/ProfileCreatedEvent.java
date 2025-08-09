package com.talentcloud.notification_ms.dto.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCreatedEvent {
    private String userId;
    private String email;
    private String firstname;
    private String profileType; // "CANDIDATE" or "CLIENT"
    private String status; // "PENDING"
    private LocalDateTime createdAt;
    private String candidateId;
    private String jobTitle;
    private String lastName;
}