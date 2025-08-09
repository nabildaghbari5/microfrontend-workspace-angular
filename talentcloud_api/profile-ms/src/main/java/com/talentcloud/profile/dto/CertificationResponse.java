package com.talentcloud.profile.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
@Getter
@Setter
public class CertificationResponse {
    private Long id;
    private String nom;
    private String organisme;
    private LocalDate dateObtention;
    private LocalDate datevalidite;
    private String urlVerification;
    private MultipartFile certificateFile;
}
