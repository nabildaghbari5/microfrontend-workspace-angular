package com.talentcloud.profile.dto.event;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCreatedEvent {
    private String userId;
    private String email;
    private String firstname; // you can leave it null if not available
    private String profileType; // "CANDIDATE" or "CLIENT"
    private String status; // should be "PENDING"
    private LocalDateTime createdAt;
    private String candidateId; // Add this to track the candidate
    private String jobTitle;    // For personalized messages
    private String lastName;
}

