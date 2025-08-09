package com.talentcloud.profile.service;

import com.talentcloud.profile.dto.UpdateEducationDto;
import com.talentcloud.profile.iservice.IServiceEducation;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.Education;
import com.talentcloud.profile.repository.CandidateRepository;
import com.talentcloud.profile.repository.EducationRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class EducationService implements IServiceEducation {

    private final EducationRepository educationRepository;
    private final CandidateRepository candidateRepository;

    public EducationService(EducationRepository educationRepository, CandidateRepository candidateRepository) {
        this.educationRepository = educationRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Validates education date logic
     * @param education the education to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEducationDates(Education education) {
        // If currently studying, no need to validate end date
        if (Boolean.TRUE.equals(education.getEnCours())) {
            return;
        }

        // If both dates are present, validate the range
        if (education.getDateDebut() != null && education.getDateFin() != null) {
            if (!education.getDateFin().isAfter(education.getDateDebut())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }

        // If not currently studying, end date should be provided
        if (Boolean.FALSE.equals(education.getEnCours()) && education.getDateFin() == null) {
            throw new IllegalArgumentException("End date is required when education is not currently ongoing");
        }
    }

    /**
     * Validates dates from UpdateEducationDto against existing education
     */
    private void validateEducationDatesFromDto(Education existingEducation, UpdateEducationDto dto) {
        // Create a temporary education object with updated values for validation
        Education tempEducation = new Education();
        tempEducation.setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : existingEducation.getDateDebut());
        tempEducation.setDateFin(dto.getDateFin() != null ? dto.getDateFin() : existingEducation.getDateFin());
        tempEducation.setEnCours(dto.getEnCours() != null ? dto.getEnCours() : existingEducation.getEnCours());

        validateEducationDates(tempEducation);
    }

    @Override
    public Education addEducation(@Valid Education education, Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));

        // Validate dates before saving
        validateEducationDates(education);

        education.setCandidate(candidate);
        education.setCreatedAt(LocalDateTime.now());

        return educationRepository.save(education);
    }

    @Override
    public Education addEducationForCurrentUser(@Valid Education education, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        // Validate dates before saving
        validateEducationDates(education);

        Candidate candidate = candidates.get(0);
        education.setCandidate(candidate);
        education.setCreatedAt(LocalDateTime.now());

        return educationRepository.save(education);
    }

    @Override
    public Education deleteEducation(Long educationId) {
        Education existingEducation = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Education not found with ID: " + educationId));

        educationRepository.delete(existingEducation); // Delete the education record
        return existingEducation; // Return the deleted education for confirmation
    }

    @Override
    public Education deleteEducationForCurrentUser(Long educationId, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the education and check if it belongs to the current user
        Education existingEducation = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Education not found with ID: " + educationId));

        if (!existingEducation.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to delete this education entry");
        }

        educationRepository.delete(existingEducation);
        return existingEducation;
    }

    @Override
    public Education editEducation(Long educationId, UpdateEducationDto dto) {
        Education existingEducation = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Education not found with ID: " + educationId));

        // Validate dates before updating
        validateEducationDatesFromDto(existingEducation, dto);

        if (dto.getInstitution() != null) existingEducation.setInstitution(dto.getInstitution());
        if (dto.getDiplome() != null) existingEducation.setDiplome(dto.getDiplome());
        if (dto.getDomaineEtude() != null) existingEducation.setDomaineEtude(dto.getDomaineEtude());
        if (dto.getDateDebut() != null) existingEducation.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) existingEducation.setDateFin(dto.getDateFin());
        if (dto.getMoyenne() != null) existingEducation.setMoyenne(dto.getMoyenne());
        if (dto.getEnCours() != null) existingEducation.setEnCours(dto.getEnCours());

        existingEducation.setUpdatedAt(LocalDateTime.now());

        return educationRepository.save(existingEducation); // Save the updated education record
    }

    @Override
    public Education editEducationForCurrentUser(Long educationId, UpdateEducationDto dto, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the education and check if it belongs to the current user
        Education existingEducation = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Education not found with ID: " + educationId));

        if (!existingEducation.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to edit this education entry");
        }

        // Validate dates before updating
        validateEducationDatesFromDto(existingEducation, dto);

        if (dto.getInstitution() != null) existingEducation.setInstitution(dto.getInstitution());
        if (dto.getDiplome() != null) existingEducation.setDiplome(dto.getDiplome());
        if (dto.getDomaineEtude() != null) existingEducation.setDomaineEtude(dto.getDomaineEtude());
        if (dto.getDateDebut() != null) existingEducation.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) existingEducation.setDateFin(dto.getDateFin());
        if (dto.getMoyenne() != null) existingEducation.setMoyenne(dto.getMoyenne());
        if (dto.getEnCours() != null) existingEducation.setEnCours(dto.getEnCours());

        existingEducation.setUpdatedAt(LocalDateTime.now());

        return educationRepository.save(existingEducation);
    }

    @Override
    public Optional<Education> getEducationById(Long educationId) {
        return educationRepository.findById(educationId);
    }

    @Override
    public Optional<Education> getEducationByIdForCurrentUser(Long educationId, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the education
        Optional<Education> education = educationRepository.findById(educationId);

        // Check if the education exists and belongs to the current user
        if (education.isPresent() && education.get().getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            return education;
        }

        return Optional.empty();
    }

    @Override
    public Page<Education> getEducationsByCandidateId(Long candidateId, Pageable pageable) {
        return educationRepository.findAllByCandidate_CandidateId(candidateId, pageable);
    }

    @Override
    public List<Education> getAllEducationsForCurrentUser(String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return educationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId());
    }

    @Override
    public Page<Education> getEducationsForCurrentUserWithPagination(String userId, Pageable pageable) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return educationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId(), pageable);
    }
}