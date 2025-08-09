package com.talentcloud.profile.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String institution;
    private String diplome;
    private String domaineEtude;

    @NotNull(message = "Start date is required")
    private LocalDate dateDebut;

    private LocalDate dateFin;
    private Double moyenne;
    private Boolean enCours;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonIgnore  // 🛑 Prevents infinite loop during serialization
    private Candidate candidate;

    /**
     * Custom validation to ensure end date is after start date
     * Only validates when both dates are present and education is not currently ongoing
     */
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        // If currently studying (enCours = true), no need to validate end date
        if (Boolean.TRUE.equals(enCours)) {
            return true;
        }

        // If either date is null, skip validation (other validations will handle nulls)
        if (dateDebut == null || dateFin == null) {
            return true;
        }

        // End date must be after start date
        return dateFin.isAfter(dateDebut);
    }
}