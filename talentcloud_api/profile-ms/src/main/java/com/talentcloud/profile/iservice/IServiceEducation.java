package com.talentcloud.profile.iservice;

import com.talentcloud.profile.dto.UpdateEducationDto;
import com.talentcloud.profile.model.Education;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IServiceEducation {
    Education addEducation(@Valid Education education, Long candidateId);

    Education addEducationForCurrentUser(@Valid Education education, String userId);

    Education deleteEducation(Long educationId);

    Education deleteEducationForCurrentUser(Long educationId, String userId);

    Education editEducation(Long educationId, UpdateEducationDto dto);

    Education editEducationForCurrentUser(Long educationId, UpdateEducationDto dto, String userId);

    Optional<Education> getEducationById(Long educationId);

    Optional<Education> getEducationByIdForCurrentUser(Long educationId, String userId);

    Page<Education> getEducationsByCandidateId(Long candidateId, Pageable pageable);

    List<Education> getAllEducationsForCurrentUser(String userId);

    Page<Education> getEducationsForCurrentUserWithPagination(String userId, Pageable pageable);
}