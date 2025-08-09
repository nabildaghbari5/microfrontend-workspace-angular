package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExperienceResponse {
    private Long id;
    private String titrePoste;
    private String entreprise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String description;
    private String lieu;
    private String typeContrat;
    private String technologies; // ✅ Change this from List<String> to String
    private Boolean enCours;
    private String siteEntreprise;
}