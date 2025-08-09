package com.talentcloud.profile.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Client {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "client_id", updatable = false, nullable = false)
    private UUID clientId;

    private String userId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String industry;

    private String address;
    private String country;

    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    private String website;
    private String linkedInUrl;
    private String logoUrl;
    @Column(name = "logo")
    private String logo;

    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    // NEW FIELDS FOR STATUS MANAGEMENT
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status")
    private ProfileStatus profileStatus = ProfileStatus.PENDING; // Default to PENDING

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    // Add blocked field
    private boolean blocked;  // Indicates whether the profile is blocked
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}