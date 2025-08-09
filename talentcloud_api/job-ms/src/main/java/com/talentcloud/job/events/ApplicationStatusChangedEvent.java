package com.talentcloud.job.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusChangedEvent extends BaseEvent {
    private Long applicationId;
    private Long jobOfferId;
    private String candidateId;
    private String candidateEmail;
    private String clientId;
    private String clientName;
    private String jobTitle;
    private String oldStatus;
    private String newStatus;
}
