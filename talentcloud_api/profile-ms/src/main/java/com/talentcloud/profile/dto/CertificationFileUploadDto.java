package com.talentcloud.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationFileUploadDto {
    private String nom;
    private String organisme;
    private LocalDate dateObtention;
    private LocalDate datevalidite;

    @URL(message = "Veuillez fournir une URL valide pour la certification")
    private String urlVerification;

    private MultipartFile certificateFile;
}