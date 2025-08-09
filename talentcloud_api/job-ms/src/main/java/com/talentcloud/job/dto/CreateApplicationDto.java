package com.talentcloud.job.dto;

import lombok.Data;

@Data
public class CreateApplicationDto {
    private Long jobOfferId;
    private String candidateId;
}