package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.Skills;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SkillsRepository extends JpaRepository<Skills, Long> {
    //List<Skills> findAllByCandidateId(Long candidateId);
    // Find skills by candidateId
    List<Skills> findByCandidate_CandidateId(Long candidateId);

    List<Skills> findAllByCandidate_CandidateId(Long candidateId);
    Page<Skills> findAllByCandidate_CandidateId(Long candidateId, Pageable pageable);
    void deleteAllByCandidate_CandidateId(Long candidateId);
    // For finding a specific skills entry by ID
    Optional<Skills> findById(Long id);

    // For pagination

    // For getting the most recent skills if needed
    Optional<Skills> findFirstByCandidate_CandidateIdOrderByCreatedAtDesc(Long candidateId);

}
