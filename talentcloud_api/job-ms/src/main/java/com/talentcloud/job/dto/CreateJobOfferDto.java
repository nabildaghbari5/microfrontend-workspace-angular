package com.talentcloud.job.dto;

import com.talentcloud.job.model.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobOfferDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private String salaryRange;

    private String requiredExperience;

    private Set<String> requiredSkills = new HashSet<>();

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;
}