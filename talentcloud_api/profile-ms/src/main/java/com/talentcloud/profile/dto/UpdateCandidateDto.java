package com.talentcloud.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talentcloud.profile.model.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateCandidateDto {
    // Existing fields
    private String profilePicture;
    private String resume;
    private String jobPreferences; // Keeping this as String as you specified
    private String jobTitle;

    // New fields
    private String aboutMe;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String phoneNumber;
    private Gender gender;
    private String linkedInUrl;
    private String portfolioUrl;
    private String address;
    private String location;

    // Add fields for related entities
    private List<EducationDto> educations;
    private List<ExperienceDto> experiences;
    private List<SkillsDto> skills;
    private List<UpdateCertificationDto> certifications;
}