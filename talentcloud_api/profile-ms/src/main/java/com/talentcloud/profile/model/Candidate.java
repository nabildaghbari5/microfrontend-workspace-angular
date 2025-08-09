package com.talentcloud.profile.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talentcloud.profile.strategy.ProfileStrategy;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;
    private String userId;
                                  // Updated: This will now store the file path instead of direct content
    private String profilePicture;
    private String resume;
    @Column(columnDefinition = "TEXT")
    private String jobPreferences;
    private String jobTitle;
    @Column(columnDefinition = "TEXT")
    private String aboutMe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String linkedInUrl;
    private String portfolioUrl;
    private String address;
    private String location; // City/Country

    @Transient
    private ProfileStrategy profileStrategy;  // Marked as transient so it is not persisted
    // NEW FIELDS FOR STATUS MANAGEMENT
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status")
    private ProfileStatus profileStatus = ProfileStatus.PENDING; // Default to PENDING

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public void applyProfileStrategy() {
        if (profileStrategy != null) {
            profileStrategy.applyStrategy(this);  // Apply the strategy on the profile
        }
    }

    public void setProfileStrategy(ProfileStrategy profileStrategy) {
        this.profileStrategy = profileStrategy;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_settings")
    private VisibilitySettings visibilitySettings;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Education> educations;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Experience> experiences;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Certification> certifications;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Skills> skills;

    private boolean blocked;  // Indicates whether the profile is blocked
}