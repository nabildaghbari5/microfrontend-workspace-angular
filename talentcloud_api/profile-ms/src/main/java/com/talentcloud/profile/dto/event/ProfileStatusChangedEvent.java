package com.talentcloud.profile.dto.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileStatusChangedEvent {
    private String userId;
    private String userEmail;
    private String userType;     // "CLIENT" or "CANDIDATE"
    private String profileStatus; // "APPROVED" or "REJECTED"
    private String message;      // Optional: helpful message like "Your profile was approved"
}
