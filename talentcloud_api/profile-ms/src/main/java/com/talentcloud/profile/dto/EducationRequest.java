 package com.talentcloud.profile.dto;


import java.time.LocalDate;

public record EducationRequest(
        String institution,
        String diplome,
        String domaineEtude,
        LocalDate dateDebut,
        LocalDate dateFin,
        Double moyenne,
        Boolean enCours
) {}

