package com.talentcloud.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long jobOfferId;
    private String candidateId; // Not Long
    private String status;
    private LocalDateTime appliedAt;
}
