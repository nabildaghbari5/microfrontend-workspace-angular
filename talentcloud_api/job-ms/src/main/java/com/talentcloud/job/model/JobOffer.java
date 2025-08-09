package com.talentcloud.job.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobOfferId;

    // Changed from Long to String to handle UUID
    private String clientId;

    private Long candidateId;

    private String title;

    @Column(length = 1000)
    private String description;

    private String location;

    private String salaryRange;

    private String requiredExperience;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> requiredSkills = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private LocalDateTime createdAt;
}