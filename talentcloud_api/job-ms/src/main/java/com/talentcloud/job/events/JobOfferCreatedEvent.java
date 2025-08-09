package com.talentcloud.job.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferCreatedEvent extends BaseEvent {
    private Long jobOfferId;
    private String clientId;
    private String jobTitle;
    private String jobDescription;
    private String location;
    private String employmentType;
}
