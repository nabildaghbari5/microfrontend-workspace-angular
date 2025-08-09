package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    Page<Experience> findByCandidate_CandidateId(Long candidateId, Pageable pageable);
    List<Experience> findByCandidate_CandidateId(Long candidateId);
    List<Experience> findAllByCandidate_CandidateId(Long candidateId);
    void deleteAllByCandidate_CandidateId(Long candidateId);
}
