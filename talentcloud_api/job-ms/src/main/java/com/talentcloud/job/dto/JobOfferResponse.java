package com.talentcloud.job.dto;

import com.talentcloud.job.model.JobOffer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobOfferResponse {
    private Long jobOfferId;
    private String clientId;
    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private String requiredExperience;
    private Set<String> requiredSkills;
    private String employmentType;
    private LocalDateTime createdAt;

    // Static helper method to convert entity to DTO
    public static JobOfferResponse fromEntity(JobOffer jobOffer) {
        JobOfferResponse response = new JobOfferResponse();
        response.setJobOfferId(jobOffer.getJobOfferId());
        response.setClientId(jobOffer.getClientId());
        response.setTitle(jobOffer.getTitle());
        response.setDescription(jobOffer.getDescription());
        response.setLocation(jobOffer.getLocation());
        response.setSalaryRange(jobOffer.getSalaryRange());
        response.setRequiredExperience(jobOffer.getRequiredExperience());
        response.setRequiredSkills(jobOffer.getRequiredSkills());
        response.setEmploymentType(jobOffer.getEmploymentType().toString());
        response.setCreatedAt(jobOffer.getCreatedAt());
        return response;
    }
}