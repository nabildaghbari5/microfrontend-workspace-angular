package com.talentcloud.job.controller;

import com.talentcloud.job.config.CandidateClient;
import com.talentcloud.job.dto.*;
import com.talentcloud.job.exception.ApplicationAlreadyExistsException;
import com.talentcloud.job.iservice.IServiceApplication;
import com.talentcloud.job.iservice.IServiceJobOffer;
import com.talentcloud.job.model.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Slf4j
@RestController
@RequestMapping("v1/applications")
public class ApplicationController {

    private final IServiceApplication applicationService;
    private final CandidateClient candidateClient;
    private final IServiceJobOffer jobOfferService;
    // In ApplicationController.java
//    CandidateResponse candidate = candidateClient.getCandidateById(rolesHeader, numericCandidateId);
    @Autowired
    public ApplicationController(
            IServiceApplication applicationService,
            CandidateClient candidateClient,
            IServiceJobOffer jobOfferService) {
        this.applicationService = applicationService;
        this.candidateClient = candidateClient;
        this.jobOfferService = jobOfferService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForJob(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String rolesHeader,
            @RequestBody CreateApplicationDto dto) {

        if (!Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only candidates can apply for jobs.");
        }

        try {
            dto.setCandidateId(userId);
            ApplicationResponse app = applicationService.createApplication(dto, userId);
            return ResponseEntity.ok(app);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ApplicationAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/job/{jobOfferId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String rolesHeader,
            @PathVariable Long jobOfferId) {

        // Check if user is client (job owner) or admin
        if (!Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get job offer to check ownership
        Optional<JobOffer> jobOffer = jobOfferService.getJobOfferById(jobOfferId);
        if (jobOffer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if client is the owner of this job
        if (Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !jobOffer.get().getClientId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get applications for the job
        List<ApplicationResponse> applications = applicationService.getApplicationsByJobId(jobOfferId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{applicationId}/candidate-details")
    public ResponseEntity<ApplicationWithCandidateDto> getApplicationWithCandidateDetails(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String rolesHeader,
            @PathVariable Long applicationId) {

        // Check if user is client (job owner) or admin
        if (!Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get application
        ApplicationResponse app = applicationService.getApplicationById(applicationId);

        // Get job offer to check ownership
        Optional<JobOffer> jobOffer = jobOfferService.getJobOfferById(app.getJobOfferId());
        if (jobOffer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if client is the owner of this job
        if (Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !jobOffer.get().getClientId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // ✅ Use UUID directly to get candidate details (no conversion needed)
            CandidateResponse candidate = candidateClient.getCandidateByUserId("Candidate", app.getCandidateId());

            // Create combined response
            ApplicationWithCandidateDto result = new ApplicationWithCandidateDto();
            result.setApplication(app);
            result.setCandidate(candidate);
            result.setJobOffer(JobOfferResponse.fromEntity(jobOffer.get()));

            return ResponseEntity.ok(result);
        } catch (feign.FeignException.NotFound e) {
            // Candidate not found
            log.warn("Candidate not found for application {}, candidateId: {}", applicationId, app.getCandidateId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (feign.codec.DecodeException e) {
            // JSON deserialization error
            log.error("Error parsing candidate data for application {}, candidateId: {}. Error: {}",
                    applicationId, app.getCandidateId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            // Log the error with more details
            log.error("Error fetching candidate details for application {}, candidateId: {}",
                    applicationId, app.getCandidateId(), e);

            // Return a 500 with appropriate error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Add this method to your ApplicationController class (copied from service)
    private Long getCandidateNumericId(String uuidCandidateId) {
        // For testing purposes, you can hardcode some mappings
        if (uuidCandidateId.equals("72eb7bb3-ad28-4803-9338-20b2182cff1a")) {
            return 1L; // Map this UUID to candidateId 1
        } else if (uuidCandidateId.equals("9dea8819-80bf-4f93-b178-33dede80ae8c")) {
            return 2L; // Map this UUID to candidateId 2
        } else {
            // Default fallback or throw exception
            return 1L; // For testing, return 1 as default
        }
    }
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String rolesHeader,
            @PathVariable Long applicationId,
            @RequestBody UpdateStatusDto statusDto) {

        // Check if user is client (job owner) or admin
        if (!Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get application to check job ownership
        ApplicationResponse app = applicationService.getApplicationById(applicationId);

        // Get job offer to check ownership
        Optional<JobOffer> jobOffer = jobOfferService.getJobOfferById(app.getJobOfferId());
        if (jobOffer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if client is the owner of this job
        if (Arrays.asList(rolesHeader.split(",")).contains("Client") &&
                !jobOffer.get().getClientId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update status
        ApplicationResponse updatedApp = applicationService.updateApplicationStatus(applicationId, statusDto.getStatus());

        return ResponseEntity.ok(updatedApp);
    }



//    @GetMapping("/candidate/{candidateId}")
//    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCandidate(
//            @RequestHeader("X-User-Id") String userId,
//            @RequestHeader("X-User-Roles") String rolesHeader,
//            @PathVariable String candidateId) {  // Changed to String for consistency
//
//        // Check if user is the candidate or admin
//        boolean isCandidate = Arrays.asList(rolesHeader.split(",")).contains("Candidate");
//        boolean isAdmin = Arrays.asList(rolesHeader.split(",")).contains("Admin");
//        boolean isOwner = userId.equals(candidateId);
//
//        if (!(isCandidate && isOwner) && !isAdmin) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//
//        // Get applications for the candidate
//        List<ApplicationResponse> applications = applicationService.getApplicationsByCandidate(candidateId);
//        return ResponseEntity.ok(applications);
//    }

    // Add a convenience method to get applications for the authenticated user
    @GetMapping("/my-applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        // Check if user is a candidate
        if (!Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get applications for the authenticated candidate
        List<ApplicationResponse> applications = applicationService.getApplicationsByCandidate(userId);
        return ResponseEntity.ok(applications);
    }
}