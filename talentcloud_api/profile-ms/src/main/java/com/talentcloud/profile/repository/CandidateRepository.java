package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
