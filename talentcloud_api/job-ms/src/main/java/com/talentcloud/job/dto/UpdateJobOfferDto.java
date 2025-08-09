package com.talentcloud.job.dto;

import com.talentcloud.job.model.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobOfferDto {
    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private String requiredExperience;
    private Set<String> requiredSkills = new HashSet<>();
    private EmploymentType employmentType;
}