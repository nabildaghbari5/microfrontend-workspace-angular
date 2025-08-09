package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Set;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    //List<Certification> findAllByCandidateId(Long candidateId);
    // Corrected method to find by candidate's relationship, not by ID
    List<Certification> findAllByCandidate_CandidateId(Long candidateId);
    List<Certification> findByCandidate(Candidate candidate);  // Correct query to find certifications for a specific candidate}
    Page<Certification> findAllByCandidate_CandidateId(Long candidateId, Pageable pageable);
    void deleteAllByCandidate_CandidateId(Long candidateId);
}