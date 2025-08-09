package com.talentcloud.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationDto {
    private String institution;
    private String diplome;
    private String domaineEtude;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double moyenne;
    private Boolean enCours;
}