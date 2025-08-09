package com.talentcloud.job.iservice;

import com.talentcloud.job.dto.CreateJobOfferDto;
import com.talentcloud.job.dto.JobOfferClientView;
import com.talentcloud.job.dto.JobOfferResponse;
import com.talentcloud.job.dto.UpdateJobOfferDto;
import com.talentcloud.job.model.JobOffer;

import java.util.List;
import java.util.Optional;

public interface IServiceJobOffer {

    // Changed parameter type from Long to String
    JobOffer createJobOffer(CreateJobOfferDto dto, String clientId);

    Optional<JobOffer> getJobOfferById(Long jobOfferId);

    // Changed parameter type from Long to String
    List<JobOffer> getJobOffersByClientId(String clientId);

    List<JobOffer> getAllJobOffers();

    JobOffer updateJobOffer(Long jobOfferId, UpdateJobOfferDto dto) throws Exception;

    void deleteJobOffer(Long jobOfferId) throws Exception;

    JobOfferResponse mapToJobOfferResponse(JobOffer jobOffer);

    /// ///
//    List<JobOfferClientView> getAllJobOfferViews();
//
//    JobOfferClientView getJobOfferViewById(Long jobOfferId);
}