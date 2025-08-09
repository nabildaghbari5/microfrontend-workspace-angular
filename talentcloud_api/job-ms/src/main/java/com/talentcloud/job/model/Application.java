package com.talentcloud.job.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobOfferId;
    private String candidateId;
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    private Status status;
}
