package com.talentcloud.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDto {
    @NotBlank(message = "Le titre du poste est obligatoire.")
    private String titrePoste;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire.")
    private String entreprise;

    @NotNull(message = "La date de début est obligatoire.")
    private LocalDate dateDebut;

    private LocalDate dateFin;

    private String description;

    private String lieu;

    @NotNull(message = "Le champ 'enCours' doit être spécifié.")
    private Boolean enCours;

    @URL(message = "Veuillez fournir une URL valide pour le site de l'entreprise.")
    private String siteEntreprise;

    @Pattern(
            regexp = "^(CDI|CDD|Freelance|Stage|Alternance)?$",
            message = "Type de contrat invalide. Les valeurs valides sont : CDI, CDD, Freelance, Stage, Alternance."
    )
    private String typeContrat;

    private String technologies;
}