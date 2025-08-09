package com.talentcloud.profile.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class EducationResponse {
    private Long id;
    private String institution;
    private String diplome;
    private String domaineEtude;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double moyenne;
    private Boolean enCours;
}
