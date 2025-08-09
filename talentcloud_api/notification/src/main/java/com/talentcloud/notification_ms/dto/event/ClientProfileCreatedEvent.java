package com.talentcloud.notification_ms.dto.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileCreatedEvent {
    private String userId;
    private String email;
    private String firstName;     // Optional
    private String lastName;      // Optional
    private String profileType;   // "CLIENT"
    private String status;        // e.g., "PENDING"
    private LocalDateTime createdAt;
    private String clientId;      // ✅ Specific to client
}

