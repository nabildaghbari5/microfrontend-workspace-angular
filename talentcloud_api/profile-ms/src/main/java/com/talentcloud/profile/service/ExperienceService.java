package com.talentcloud.profile.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.talentcloud.profile.dto.UpdateExperienceDto;
import com.talentcloud.profile.iservice.IServiceExperience;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.Experience;
import com.talentcloud.profile.repository.CandidateRepository;
import com.talentcloud.profile.repository.ExperienceRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class ExperienceService implements IServiceExperience {

    private final ExperienceRepository experienceRepository;
    private final CandidateRepository candidateRepository;

    public ExperienceService(ExperienceRepository experienceRepository, CandidateRepository candidateRepository) {
        this.experienceRepository = experienceRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Validates experience date logic
     * @param experience the experience to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateExperienceDates(Experience experience) {
        // If currently working, no need to validate end date
        if (Boolean.TRUE.equals(experience.getEnCours())) {
            return;
        }

        // If both dates are present, validate the range
        if (experience.getDateDebut() != null && experience.getDateFin() != null) {
            if (!experience.getDateFin().isAfter(experience.getDateDebut())) {
                throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
            }
        }

        // If not currently working, end date should be provided
        if (Boolean.FALSE.equals(experience.getEnCours()) && experience.getDateFin() == null) {
            throw new IllegalArgumentException("La date de fin est obligatoire lorsque l'expérience n'est pas en cours");
        }
    }

    /**
     * Validates dates from UpdateExperienceDto against existing experience
     */
    private void validateExperienceDatesFromDto(Experience existingExperience, UpdateExperienceDto dto) {
        // Create a temporary experience object with updated values for validation
        Experience tempExperience = new Experience();
        tempExperience.setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : existingExperience.getDateDebut());
        tempExperience.setDateFin(dto.getDateFin() != null ? dto.getDateFin() : existingExperience.getDateFin());
        tempExperience.setEnCours(dto.getEnCours() != null ? dto.getEnCours() : existingExperience.getEnCours());

        validateExperienceDates(tempExperience);
    }

    @Override
    public Experience createExperience(@Valid Experience experience,@NotNull Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));

        // Validate dates before saving
        validateExperienceDates(experience);

        experience.setCandidate(candidate);
        experience.setCreatedAt(LocalDateTime.now());
        experience.setUpdatedAt(LocalDateTime.now());

        return experienceRepository.save(experience);
    }

    @Override
    public Experience createExperienceForCurrentUser(@Valid Experience experience,@NotNull String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        // Validate dates before saving
        validateExperienceDates(experience);

        Candidate candidate = candidates.get(0);

        experience.setCandidate(candidate);
        experience.setCreatedAt(LocalDateTime.now());
        experience.setUpdatedAt(LocalDateTime.now());

        return experienceRepository.save(experience);
    }

    @Override
    public Experience updateExperience(@NotNull Long experienceId, UpdateExperienceDto dto) {
        Experience existing = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found with id: " + experienceId));

        // Validate dates before updating
        validateExperienceDatesFromDto(existing, dto);

        if (dto.getTitrePoste() != null) existing.setTitrePoste(dto.getTitrePoste());
        if (dto.getEntreprise() != null) existing.setEntreprise(dto.getEntreprise());
        if (dto.getDateDebut() != null) existing.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) existing.setDateFin(dto.getDateFin());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getLieu() != null) existing.setLieu(dto.getLieu());
        if (dto.getEnCours() != null) existing.setEnCours(dto.getEnCours());
        if (dto.getSiteEntreprise() != null) existing.setSiteEntreprise(dto.getSiteEntreprise());
        if (dto.getTypeContrat() != null) existing.setTypeContrat(dto.getTypeContrat());
        if (dto.getTechnologies() != null) existing.setTechnologies(dto.getTechnologies());

        existing.setUpdatedAt(LocalDateTime.now());

        return experienceRepository.save(existing);
    }

    @Override
    public Experience updateExperienceForCurrentUser(@NotNull Long experienceId, UpdateExperienceDto dto, @NotNull String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the experience and check if it belongs to the current user
        Experience existing = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found with id: " + experienceId));

        if (!existing.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to update this experience entry");
        }

        // Validate dates before updating
        validateExperienceDatesFromDto(existing, dto);

        if (dto.getTitrePoste() != null) existing.setTitrePoste(dto.getTitrePoste());
        if (dto.getEntreprise() != null) existing.setEntreprise(dto.getEntreprise());
        if (dto.getDateDebut() != null) existing.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) existing.setDateFin(dto.getDateFin());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getLieu() != null) existing.setLieu(dto.getLieu());
        if (dto.getEnCours() != null) existing.setEnCours(dto.getEnCours());
        if (dto.getSiteEntreprise() != null) existing.setSiteEntreprise(dto.getSiteEntreprise());
        if (dto.getTypeContrat() != null) existing.setTypeContrat(dto.getTypeContrat());
        if (dto.getTechnologies() != null) existing.setTechnologies(dto.getTechnologies());

        existing.setUpdatedAt(LocalDateTime.now());

        return experienceRepository.save(existing);
    }

    @Override
    public Experience deleteExperience(@NotNull Long experienceId) {
        Experience existing = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found with id: " + experienceId));

        experienceRepository.delete(existing);
        return existing;
    }

    @Override
    public Experience deleteExperienceForCurrentUser(@NotNull Long experienceId,@NotNull String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the experience and check if it belongs to the current user
        Experience existing = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found with id: " + experienceId));

        if (!existing.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to delete this experience entry");
        }

        experienceRepository.delete(existing);
        return existing;
    }

    @Override
    public Optional<Experience> getExperienceById(@NotNull Long experienceId) {
        return experienceRepository.findById(experienceId);
    }

    @Override
    public Optional<Experience> getExperienceByIdForCurrentUser(@NotNull Long experienceId, @NotNull  String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the experience
        Optional<Experience> experience = experienceRepository.findById(experienceId);

        // Check if the experience exists and belongs to the current user
        if (experience.isPresent() && experience.get().getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            return experience;
        }

        return Optional.empty();
    }

    @Override
    public Page<Experience> getExperiencesByCandidateId(@NotNull Long candidateId, Pageable pageable) {
        return experienceRepository.findByCandidate_CandidateId(candidateId, pageable);
    }

    @Override
    public List<Experience> getAllExperiencesForCurrentUser(@NotNull String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return experienceRepository.findByCandidate_CandidateId(candidate.getCandidateId());
    }

    @Override
    public Page<Experience> getExperiencesForCurrentUserWithPagination(@NotNull String userId, Pageable pageable) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return experienceRepository.findByCandidate_CandidateId(candidate.getCandidateId(), pageable);
    }
}