package com.talentcloud.job.dto;

import com.talentcloud.job.model.EmploymentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobOfferClientView {

    // From Client (profile-ms)
    private String companyName;
    private String logo;
    private String companyDescription;

    // From JobOffer (job-ms)
    private Long jobOfferId;
    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private String requiredExperience;
    private Set<String> requiredSkills;
    private EmploymentType employmentType;
    private LocalDateTime createdAt;
}
