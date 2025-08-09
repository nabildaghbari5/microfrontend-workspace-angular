package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;


public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findAllByCandidate_CandidateId(Long candidateId);
    Page<Education> findAllByCandidate_CandidateId(Long candidateId, Pageable pageable);
    void deleteAllByCandidate_CandidateId(Long candidateId);

    //   Set<Education> findAllByCandidateId(Long candidateId);
    //List<Education> findAllByCandidateId(Long candidateId);



}
