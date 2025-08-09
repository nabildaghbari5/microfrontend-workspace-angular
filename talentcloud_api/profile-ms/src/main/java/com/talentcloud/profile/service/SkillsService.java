package com.talentcloud.profile.service;

import com.talentcloud.profile.iservice.IServiceSkills;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.Skills;
import com.talentcloud.profile.dto.UpdateSkillsDto;
import com.talentcloud.profile.repository.SkillsRepository;
import com.talentcloud.profile.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SkillsService implements IServiceSkills {

    private final SkillsRepository skillsRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public SkillsService(SkillsRepository skillsRepository, CandidateRepository candidateRepository) {
        this.skillsRepository = skillsRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public List<Skills> getSkillsByCandidateId(Long candidateId) {
        return skillsRepository.findByCandidate_CandidateId(candidateId);
    }

    @Override
    public List<Skills> getSkillsForCurrentUser(String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return skillsRepository.findByCandidate_CandidateId(candidate.getCandidateId());
    }

    @Override
    public Optional<Skills> getMostRecentSkillsForCurrentUser(String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return skillsRepository.findFirstByCandidate_CandidateIdOrderByCreatedAtDesc(candidate.getCandidateId());
    }

    @Override
    public Optional<Skills> getSkillsById(Long skillsId) {
        return skillsRepository.findById(skillsId);
    }

    @Override
    public Optional<Skills> getSkillsByIdForCurrentUser(Long skillsId, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the skills
        Optional<Skills> skills = skillsRepository.findById(skillsId);

        // Check if the skills exist and belong to the current user
        if (skills.isPresent() && skills.get().getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            return skills;
        }

        return Optional.empty();
    }

    @Override
    public Page<Skills> getAllSkillsByCandidateId(Long candidateId, Pageable pageable) {
        return skillsRepository.findAllByCandidate_CandidateId(candidateId, pageable);
    }

    @Override
    public Page<Skills> getAllSkillsForCurrentUserPaginated(String userId, Pageable pageable) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        return skillsRepository.findAllByCandidate_CandidateId(candidate.getCandidateId(), pageable);
    }

    @Override
    public Skills addSkills(Skills skills, Long candidateId) {
        return candidateRepository.findById(candidateId)
                .map(candidate -> {
                    skills.setCandidate(candidate);
                    skills.setCreatedAt(LocalDateTime.now());
                    return skillsRepository.save(skills);
                })
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));
    }

    @Override
    public Skills addSkillsForCurrentUser(Skills skills, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        skills.setCandidate(candidate);
        skills.setCreatedAt(LocalDateTime.now());

        return skillsRepository.save(skills);
    }

    @Override
    public Skills updateSkills(Long skillsId, UpdateSkillsDto updateSkillsDto) {
        Skills existingSkills = skillsRepository.findById(skillsId)
                .orElseThrow(() -> new IllegalArgumentException("Skills not found with id: " + skillsId));

        // Update the skills with data from the DTO
        existingSkills.setProgrammingLanguages(updateSkillsDto.getProgrammingLanguages());
        existingSkills.setSoftSkills(updateSkillsDto.getSoftSkills());
        existingSkills.setTechnicalSkills(updateSkillsDto.getTechnicalSkills());
        existingSkills.setToolsAndTechnologies(updateSkillsDto.getToolsAndTechnologies());
        existingSkills.setCustomSkills(updateSkillsDto.getCustomSkills());

        // Set the updatedAt timestamp to the current time
        existingSkills.setUpdatedAt(LocalDateTime.now());

        // Save and return the updated skills
        return skillsRepository.save(existingSkills);
    }

    @Override
    public Skills updateSkillsForCurrentUser(Long skillsId, UpdateSkillsDto updateSkillsDto, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);

        // Find the skills and check if it belongs to the current user
        Skills existingSkills = skillsRepository.findById(skillsId)
                .orElseThrow(() -> new IllegalArgumentException("Skills not found with id: " + skillsId));

        if (!existingSkills.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to update these skills");
        }

        // Update the skills with data from the DTO
        existingSkills.setProgrammingLanguages(updateSkillsDto.getProgrammingLanguages());
        existingSkills.setSoftSkills(updateSkillsDto.getSoftSkills());
        existingSkills.setTechnicalSkills(updateSkillsDto.getTechnicalSkills());
        existingSkills.setToolsAndTechnologies(updateSkillsDto.getToolsAndTechnologies());
        existingSkills.setCustomSkills(updateSkillsDto.getCustomSkills());

        // Set the updatedAt timestamp to the current time
        existingSkills.setUpdatedAt(LocalDateTime.now());

        // Save and return the updated skills
        return skillsRepository.save(existingSkills);
    }

    @Override
    public void deleteSkills(Long skillsId) {
        Skills existingSkills = skillsRepository.findById(skillsId)
                .orElseThrow(() -> new IllegalArgumentException("Skills not found with id: " + skillsId));

        skillsRepository.delete(existingSkills);
    }

    @Override
    public void deleteSkillsForCurrentUser(Long skillsId, String userId) {
        // Find the candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }

        Candidate candidate = candidates.get(0);


        Skills existingSkills = skillsRepository.findById(skillsId)
                .orElseThrow(() -> new IllegalArgumentException("Skills not found with id: " + skillsId));

        if (!existingSkills.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to delete these skills");
        }

        skillsRepository.delete(existingSkills);
    }
}