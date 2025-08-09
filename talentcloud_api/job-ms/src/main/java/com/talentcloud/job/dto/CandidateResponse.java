package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {

    private Long candidateId;
    private String userId;
    private String profilePicture;
    private String resume;
    private String jobPreferences;
    private String jobTitle;

    // Personal information
    private String firstName;
    private String lastName;
    private String email;
    private String aboutMe;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String gender;

    // Social and professional links
    private String linkedInUrl;
    private String portfolioUrl;

    // Location information
    private String address;
    private String location;

    // Related information (these would be populated by the profile-ms service)
    private List<EducationResponse> educations;
    private List<ExperienceResponse> experiences;
    private List<CertificationResponse> certifications;
    private List<SkillsResponse> skills;
}