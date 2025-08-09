package com.talentcloud.job.service;

import com.talentcloud.job.config.CandidateClient;
import com.talentcloud.job.dto.*;
import com.talentcloud.job.events.JobOfferCreatedEvent;
import com.talentcloud.job.exception.JobOfferNotFoundException;
import com.talentcloud.job.iservice.IServiceJobOffer;
import com.talentcloud.job.model.JobOffer;
import com.talentcloud.job.repository.JobOfferRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class JobOfferService implements IServiceJobOffer {

    private final JobOfferRepository jobOfferRepository;
    private final NotificationService notificationService;
    private final CandidateClient candidateClient;


    @Autowired
    public JobOfferService(JobOfferRepository jobOfferRepository, NotificationService notificationService, CandidateClient candidateClient) {
        this.jobOfferRepository = jobOfferRepository;
        this.notificationService = notificationService;
        this.candidateClient = candidateClient;
    }

    @Override
    @Transactional
    public JobOffer createJobOffer(CreateJobOfferDto dto, String clientId) {
        // 🔍 Vérifier le statut du profil client via Feign
        String profileStatus = candidateClient.getClientProfileStatus(clientId, "CLIENT");

        if (!"APPROVED".equalsIgnoreCase(profileStatus)) {
            throw new IllegalStateException("Your profile is " + profileStatus + ". You must be APPROVED to create a job offer.");
        }

        JobOffer jobOffer = JobOffer.builder()
                .clientId(clientId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .salaryRange(dto.getSalaryRange())
                .requiredExperience(dto.getRequiredExperience())
                .requiredSkills(dto.getRequiredSkills())
                .employmentType(dto.getEmploymentType())
                .createdAt(LocalDateTime.now())
                .build();

        JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);

        // 📨 Kafka event
        try {
            JobOfferCreatedEvent event = JobOfferCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .eventType("JOB_OFFER_CREATED")
                    .jobOfferId(savedJobOffer.getJobOfferId())
                    .clientId(savedJobOffer.getClientId())
                    .jobTitle(savedJobOffer.getTitle())
                    .jobDescription(savedJobOffer.getDescription())
                    .location(savedJobOffer.getLocation())
                    .employmentType(savedJobOffer.getEmploymentType().toString())
                    .build();

            notificationService.sendJobCreatedEvent(event);
        } catch (Exception e) {
            log.error("Failed to send job created event, but job was saved successfully", e);
        }

        return savedJobOffer;
    }


    @Override
    @Transactional
    public Optional<JobOffer> getJobOfferById(Long jobOfferId) {
        return jobOfferRepository.findById(jobOfferId);
    }

    @Override
    @Transactional
    public List<JobOffer> getJobOffersByClientId(String clientId) {
        return jobOfferRepository.findByClientId(clientId);
    }

    @Override
    @Transactional
    public List<JobOffer> getAllJobOffers() {
        return jobOfferRepository.findAll();
    }

    @Override
    @Transactional
    public JobOffer updateJobOffer(Long jobOfferId, UpdateJobOfferDto dto) throws Exception {
        JobOffer existingJobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with id " + jobOfferId));
        if (dto.getTitle() != null) existingJobOffer.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existingJobOffer.setDescription(dto.getDescription());
        if (dto.getLocation() != null) existingJobOffer.setLocation(dto.getLocation());
        if (dto.getSalaryRange() != null) existingJobOffer.setSalaryRange(dto.getSalaryRange());
        if (dto.getRequiredExperience() != null) existingJobOffer.setRequiredExperience(dto.getRequiredExperience());
        if (dto.getRequiredSkills() != null && !dto.getRequiredSkills().isEmpty()) {
            existingJobOffer.setRequiredSkills(dto.getRequiredSkills());
        }
        if (dto.getEmploymentType() != null) existingJobOffer.setEmploymentType(dto.getEmploymentType());

        return jobOfferRepository.save(existingJobOffer);
    }

    @Override
    @Transactional
    public void deleteJobOffer(Long jobOfferId) throws Exception {
        JobOffer existingJobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with id " + jobOfferId));

        jobOfferRepository.delete(existingJobOffer);
    }

    @Override
    public JobOfferResponse mapToJobOfferResponse(JobOffer jobOffer) {
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



    /// //
//    @Override
//    public List<JobOfferClientView> getAllJobOfferViews() {
//        List<JobOffer> jobOffers = jobOfferRepository.findAll();
//        List<JobOfferClientView> views = new ArrayList<>();
//
//        for (JobOffer offer : jobOffers) {
//            views.add(buildJobOfferClientView(offer));
//        }
//
//        return views;
//    }
//
//    @Override
//    public JobOfferClientView getJobOfferViewById(Long jobOfferId) {
//        Optional<JobOffer> optionalJobOffer = jobOfferRepository.findById(jobOfferId);
//        return optionalJobOffer.map(this::buildJobOfferClientView).orElse(null);
//    }
//
//    private JobOfferClientView buildJobOfferClientView(JobOffer offer) {
//        ClientDto client = candidateClient.getClientById(offer.getClientId());
//
//        JobOfferClientView view = new JobOfferClientView();
//
//        // JobOffer data
//        view.setJobOfferId(offer.getJobOfferId());
//        view.setTitle(offer.getTitle());
//        view.setDescription(offer.getDescription());
//        view.setLocation(offer.getLocation());
//        view.setSalaryRange(offer.getSalaryRange());
//        view.setRequiredExperience(offer.getRequiredExperience());
//        view.setRequiredSkills(offer.getRequiredSkills());
//        view.setEmploymentType(offer.getEmploymentType());
//        view.setCreatedAt(offer.getCreatedAt());
//
//        // Client data
//        if (client != null) {
//            view.setCompanyName(client.getCompanyName());
//            view.setLogo(client.getLogo());
//            view.setCompanyDescription(client.getCompanyDescription());
//        }
//
//        return view;
//    }
}