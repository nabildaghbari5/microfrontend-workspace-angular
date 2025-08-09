package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
class EducationResponse {
    private Long id;
    private String diplome;
    private String institution;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean enCours;
    private Double moyenne;
    private String domaineEtude;
}
