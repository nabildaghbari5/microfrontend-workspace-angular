package com.talentcloud.profile.dto;

import com.talentcloud.profile.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CandidateResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;
    private String userId; // Add this field if it doesn't exist

    private String profilePicture;

    private String resume;

    private String jobPreferences;

    private String jobTitle;

    private String aboutMe;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String gender;

    private String linkedInUrl;
    private String portfolioUrl;
    private String address;
    private String location;

    private List<EducationResponse> educations;
    private List<ExperienceResponse> experiences;
    private List<CertificationResponse> certifications;
    private List<SkillsResponse> skills;
    private String visibilitySettings;

}
