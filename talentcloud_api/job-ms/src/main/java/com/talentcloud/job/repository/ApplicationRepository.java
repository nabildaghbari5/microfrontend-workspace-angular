package com.talentcloud.job.repository;

import com.talentcloud.job.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobOfferId(Long jobOfferId);
    List<Application> findByCandidateId(String candidateId);
    boolean existsByJobOfferIdAndCandidateId(Long jobOfferId, String candidateId);

}