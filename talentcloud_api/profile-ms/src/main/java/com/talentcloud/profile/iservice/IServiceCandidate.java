package com.talentcloud.profile.iservice;

import com.talentcloud.profile.dto.CandidateResponse;
import com.talentcloud.profile.dto.CreateProfileDto;
import com.talentcloud.profile.dto.UpdateCandidateDto;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.VisibilitySettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IServiceCandidate {

//    Candidate createCompleteProfile(CreateProfileDto request, String userId, String username, String email) throws Exception;
    CandidateResponse findCandidateByUserId(String userId);

    // Add new method that accepts userId from header
    Candidate createCandidateProfile(@Valid Candidate candidate, @NotNull String userId);

    // Find candidate by ID for response mapping
    CandidateResponse findCandidateById(@NotNull Long candidateId);

    // Profile management methods
    Candidate blockProfile(@NotNull Long candidateId) throws Exception;

    Candidate editCandidateProfile(@NotNull Long candidateId, @Valid UpdateCandidateDto dto) throws Exception;

    // Get candidate by ID (returns Optional)
    Optional<Candidate> getCandidateById(@NotNull Long candidateId);

    // Get all candidates
    List<Candidate> getAllCandidates();

    // Update visibility settings
    Candidate updateVisibility(@NotNull Long candidateId, @NotNull VisibilitySettings visibility) throws Exception;

    // Find candidates by user ID
    List<Candidate> findByUserId(@NotNull String userId);

    // Delete candidate method
    void deleteCandidate(@NotNull Long candidateId) throws Exception;

    // STATUS MANAGEMENT METHODS
    Candidate approveCandidate(@NotNull Long candidateId) throws Exception;

    Candidate rejectCandidate(@NotNull Long candidateId, @NotNull String rejectionReason) throws Exception;
    // NEW METHOD: Upload profile picture
    String uploadProfilePicture(@NotNull String userId, @NotNull MultipartFile file) throws Exception;

    Candidate createCompleteProfile(@Valid CreateProfileDto request, String userId) throws Exception;

    Optional<Candidate> getCandidateByUserId(String userId);

}