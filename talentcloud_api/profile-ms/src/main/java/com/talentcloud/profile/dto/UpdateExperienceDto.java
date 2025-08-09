package com.talentcloud.profile.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateExperienceDto {
    private String titrePoste;
    private String entreprise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String description;
    private String lieu;
    private Boolean enCours;
    private String siteEntreprise;
    private String typeContrat;
    private String technologies;
}

