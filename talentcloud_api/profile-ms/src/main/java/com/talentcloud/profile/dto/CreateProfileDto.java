package com.talentcloud.profile.dto;

import com.talentcloud.profile.model.Gender;
import com.talentcloud.profile.model.VisibilitySettings;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileDto {
    // Candidate information
    private String aboutMe;
    private String jobTitle;
    private String jobPreferences;
    private String profilePicture;
    private String resume;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Gender gender;
    private String linkedInUrl;
    private String portfolioUrl;
    private String address;
    private String location;
    private VisibilitySettings visibilitySettings;

    // Related entities
    @Valid
    private List<EducationDto> educations;

    @Valid
    private List<ExperienceDto> experiences;

    @Valid
    private SkillsDto skills;

    // Add certifications list with validation
//    @Valid
//    private List<CertificationFileUploadDto> certifications;
}
