package com.talentcloud.job.repository;

import com.talentcloud.job.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    // Changed parameter type from Long to String
    List<JobOffer> findByClientId(String clientId);
}