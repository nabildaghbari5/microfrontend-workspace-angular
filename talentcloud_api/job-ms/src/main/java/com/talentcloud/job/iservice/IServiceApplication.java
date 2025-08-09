package com.talentcloud.job.iservice;

import com.talentcloud.job.dto.ApplicationResponse;
import com.talentcloud.job.dto.CreateApplicationDto;
import com.talentcloud.job.model.Status;

import java.util.List;

public interface IServiceApplication {

    /**
     * Create a new application for a job
     *
     * @param dto the application data transfer object
     * @param userId the user ID of the applicant (will be used as candidateId)
     * @return the created application response
     */
    ApplicationResponse createApplication(CreateApplicationDto dto, String userId);

    /**
     * Get an application by its ID
     *
     * @param applicationId the ID of the application
     * @return the application response
     */
    ApplicationResponse getApplicationById(Long applicationId);

    /**
     * Get all applications for a specific job
     *
     * @param jobOfferId the ID of the job offer
     * @return a list of application responses
     */
    List<ApplicationResponse> getApplicationsByJobId(Long jobOfferId);

    /**
     * Get all applications submitted by a candidate
     *
     * @param candidateId the ID of the candidate
     * @return a list of application responses
     */
    List<ApplicationResponse> getApplicationsByCandidate(String candidateId);

    /**
     * Update the status of an application
     *
     * @param applicationId the ID of the application
     * @param status the new status
     * @return the updated application response
     */
    ApplicationResponse updateApplicationStatus(Long applicationId, Status status);
}