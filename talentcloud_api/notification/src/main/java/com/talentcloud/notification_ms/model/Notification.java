package com.talentcloud.notification_ms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String email;

    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationState state;

    private LocalDateTime createdAt;
}
