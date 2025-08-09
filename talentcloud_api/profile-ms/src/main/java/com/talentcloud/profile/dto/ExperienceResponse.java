package com.talentcloud.profile.dto;



import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class ExperienceResponse {
    private Long id;
    private String titrePoste;
    private String entreprise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String description;
    private String lieu;
    private String typeContrat;
    private String technologies;
    private Boolean enCours;
    private String siteEntreprise;
}
