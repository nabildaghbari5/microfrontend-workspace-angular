package com.talentcloud.profile.iservice;

import com.talentcloud.profile.model.Skills;
import com.talentcloud.profile.dto.UpdateSkillsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IServiceSkills {
    // Original methods
    List<Skills> getSkillsByCandidateId(Long candidateId); // Changed to return List
    Optional<Skills> getSkillsById(Long skillsId);
    Page<Skills> getAllSkillsByCandidateId(Long candidateId, Pageable pageable);
    Skills addSkills(Skills skills, Long candidateId);
    Skills updateSkills(Long skillsId, UpdateSkillsDto updateSkillsDto);
    void deleteSkills(Long skillsId);

    // New methods using userId from header - modified to return List
    List<Skills> getSkillsForCurrentUser(String userId);
    Optional<Skills> getSkillsByIdForCurrentUser(Long skillsId, String userId);
    Page<Skills> getAllSkillsForCurrentUserPaginated(String userId, Pageable pageable);
    Skills addSkillsForCurrentUser(Skills skills, String userId);
    Skills updateSkillsForCurrentUser(Long skillsId, UpdateSkillsDto updateSkillsDto, String userId);
    void deleteSkillsForCurrentUser(Long skillsId, String userId);

    // Optional: Method to get only the most recent skills
    Optional<Skills> getMostRecentSkillsForCurrentUser(String userId);
}