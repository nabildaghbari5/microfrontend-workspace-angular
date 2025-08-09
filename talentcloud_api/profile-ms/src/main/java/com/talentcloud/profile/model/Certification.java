package com.talentcloud.profile.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String organisme;
    private LocalDate dateObtention;
    private LocalDate datevalidite;

    @URL(message = "Veuillez fournir une URL valide pour la certification")
    private String urlVerification;

    // New field for storing the certification file path
    private String certificateFilePath;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonIgnore  // 🛑 Prevents infinite loop during serialization
    private Candidate candidate;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(insertable = false)
    private LocalDateTime updatedAt;
}