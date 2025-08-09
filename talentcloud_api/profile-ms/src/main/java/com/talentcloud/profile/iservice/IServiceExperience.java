package com.talentcloud.profile.iservice;

import com.talentcloud.profile.dto.UpdateExperienceDto;
import com.talentcloud.profile.model.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public interface IServiceExperience {
    // Original methods - ADD @Valid annotations to match implementation
    Experience createExperience(@Valid Experience experience, @NotNull Long candidateId);
    Experience updateExperience(@NotNull Long experienceId, UpdateExperienceDto dto);
    Experience deleteExperience(@NotNull Long experienceId);
    Optional<Experience> getExperienceById(@NotNull Long experienceId);
    Page<Experience> getExperiencesByCandidateId(@NotNull Long candidateId, Pageable pageable);

    // New methods using userId from header - ADD @Valid annotations to match implementation
    Experience createExperienceForCurrentUser(@Valid Experience experience, @NotNull String userId);
    Experience updateExperienceForCurrentUser(@NotNull Long experienceId, UpdateExperienceDto dto, @NotNull String userId);
    Experience deleteExperienceForCurrentUser(@NotNull Long experienceId, @NotNull String userId);
    Optional<Experience> getExperienceByIdForCurrentUser(@NotNull Long experienceId, @NotNull String userId);
    List<Experience> getAllExperiencesForCurrentUser(@NotNull String userId);
    Page<Experience> getExperiencesForCurrentUserWithPagination(@NotNull String userId, Pageable pageable);
}