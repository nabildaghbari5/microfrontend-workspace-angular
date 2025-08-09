package com.talentcloud.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationWithCandidateDto {
    private ApplicationResponse application;
    private CandidateResponse candidate;
    private JobOfferResponse jobOffer;
}