package com.talentcloud.job.service;

import com.talentcloud.job.config.CandidateClient;
import com.talentcloud.job.dto.ApplicationResponse;
import com.talentcloud.job.dto.CandidateResponse;
import com.talentcloud.job.dto.CreateApplicationDto;
import com.talentcloud.job.events.ApplicationStatusChangedEvent;
import com.talentcloud.job.events.ApplicationSubmittedEvent;
import com.talentcloud.job.exception.ApplicationAlreadyExistsException;
import com.talentcloud.job.exception.ApplicationNotFoundException;
import com.talentcloud.job.exception.ResourceNotFoundException;
import com.talentcloud.job.iservice.IServiceApplication;
import com.talentcloud.job.model.Application;
import com.talentcloud.job.model.JobOffer;
import com.talentcloud.job.model.Status;
import com.talentcloud.job.repository.ApplicationRepository;
import com.talentcloud.job.repository.JobOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApplicationService implements IServiceApplication {

    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final NotificationService notificationService;
    private final CandidateClient candidateClient;

    @Autowired
    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobOfferRepository jobOfferRepository,
            NotificationService notificationService,
            CandidateClient candidateClient) {
        this.applicationRepository = applicationRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.notificationService = notificationService;
        this.candidateClient = candidateClient;
    }

    @Override
    @Transactional
    public ApplicationResponse createApplication(CreateApplicationDto dto, String userId) {
        // Vérifier le profil du candidat via OpenFeign
        String profileStatus = candidateClient.getCandidateProfileStatus(userId, "CANDIDATE");

        if (!"APPROVED".equalsIgnoreCase(profileStatus)) {
            throw new IllegalStateException("Your profile is " + profileStatus + ". You must be APPROVED to apply for a job.");
        }

        JobOffer jobOffer = jobOfferRepository.findById(dto.getJobOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found with id: " + dto.getJobOfferId()));

        if (applicationRepository.existsByJobOfferIdAndCandidateId(dto.getJobOfferId(), userId)) {
            throw new ApplicationAlreadyExistsException("You have already applied for this job");
        }

        Application application = Application.builder()
                .jobOfferId(dto.getJobOfferId())
                .candidateId(userId)
                .status(Status.SUBMITTED)
                .appliedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);

        try {
            CandidateResponse candidate = getCandidateByUserId(userId);
            Map<String, String> response = candidateClient.getClientEmailByUserId(jobOffer.getClientId(), "CLIENT");
            String clientEmail = response.getOrDefault("email", "client@email.com");

            ApplicationSubmittedEvent event = ApplicationSubmittedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .eventType("APPLICATION_SUBMITTED")
                    .applicationId(saved.getId())
                    .jobOfferId(saved.getJobOfferId())
                    .candidateId(saved.getCandidateId())
                    .candidateName(candidate.getFirstName() + " " + candidate.getLastName())
                    .candidateEmail(candidate.getEmail())
                    .clientId(jobOffer.getClientId())
                    .clientEmail(clientEmail)
                    .jobTitle(jobOffer.getTitle())
                    .build();

            notificationService.sendApplicationSubmittedEvent(event);
        } catch (Exception e) {
            log.error("Failed to send application submitted event, but application was saved successfully", e);
        }

        return mapToApplicationResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));
        return mapToApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJobId(Long jobOfferId) {
        if (!jobOfferRepository.existsById(jobOfferId)) {
            throw new ResourceNotFoundException("Job offer not found with id: " + jobOfferId);
        }
        return applicationRepository.findByJobOfferId(jobOfferId)
                .stream().map(this::mapToApplicationResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCandidate(String candidateId) {
        return applicationRepository.findByCandidateId(candidateId)
                .stream().map(this::mapToApplicationResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, Status status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + applicationId));

        String oldStatus = application.getStatus().toString();
        application.setStatus(status);
        Application updated = applicationRepository.save(application);

        JobOffer jobOffer = jobOfferRepository.findById(application.getJobOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found"));

        try {
            // Get candidate details using the new UUID-based method
            CandidateResponse candidate = getCandidateByUserId(application.getCandidateId());

            // Get client email
            Map<String, String> response = candidateClient.getClientEmailByUserId(jobOffer.getClientId(), "CLIENT");
            String clientEmail = response.getOrDefault("email", "client@email.com");

            ApplicationStatusChangedEvent event = ApplicationStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .eventType("APPLICATION_STATUS_CHANGED")
                    .applicationId(updated.getId())
                    .jobOfferId(updated.getJobOfferId())
                    .candidateId(updated.getCandidateId())
                    .candidateEmail(candidate.getEmail())
                    .clientId(jobOffer.getClientId())
                    .clientName("Client Name") // Consider fetching this too later
                    .jobTitle(jobOffer.getTitle())
                    .oldStatus(oldStatus)
                    .newStatus(status.toString())
                    .build();

            notificationService.sendApplicationStatusChangedEvent(event);
        } catch (Exception e) {
            log.error("Failed to send application status changed event, but status was updated successfully", e);
        }

        return mapToApplicationResponse(updated);
    }

    /**
     * Get candidate details by userId (UUID from auth token)
     * This method replaces the old hardcoded mapping approach
     */
    private CandidateResponse getCandidateByUserId(String candidateUserId) {
        try {
            log.info("Attempting to fetch candidate with userId: {}", candidateUserId);
            CandidateResponse candidate = candidateClient.getCandidateByUserId("Candidate", candidateUserId);
            log.info("Successfully fetched candidate: {} {}", candidate.getFirstName(), candidate.getLastName());
            return candidate;
        } catch (feign.FeignException.NotFound e) {
            log.warn("Candidate not found for userId: {}", candidateUserId);
            throw new ResourceNotFoundException("Candidate not found for userId: " + candidateUserId);
        } catch (feign.codec.DecodeException e) {
            log.error("JSON deserialization error for userId: {}. Error: {}", candidateUserId, e.getMessage());
            throw new ResourceNotFoundException("Error parsing candidate data for userId: " + candidateUserId);
        } catch (Exception e) {
            log.error("Unexpected error fetching candidate by userId: {}", candidateUserId, e);
            throw new ResourceNotFoundException("Failed to fetch candidate for userId: " + candidateUserId);
        }
    }

    // REMOVED: getCandidateNumericId method - no longer needed!

    private ApplicationResponse mapToApplicationResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobOfferId(application.getJobOfferId());
        response.setCandidateId(application.getCandidateId());
        response.setStatus(application.getStatus().toString());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }
}