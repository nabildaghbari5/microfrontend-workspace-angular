package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
class CertificationResponse {
    private Long id;
    private String nom;
    private String organisme;
    private LocalDate dateObtention;
    private LocalDate datevalidite;
    private String urlVerification;
}
