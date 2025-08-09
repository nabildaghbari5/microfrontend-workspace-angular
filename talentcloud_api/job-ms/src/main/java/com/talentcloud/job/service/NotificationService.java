package com.talentcloud.job.service;

import com.talentcloud.job.events.ApplicationStatusChangedEvent;
import com.talentcloud.job.events.ApplicationSubmittedEvent;
import com.talentcloud.job.events.JobOfferCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.job-created}")
    private String jobCreatedTopic;

    @Value("${kafka.topics.application-submitted}")
    private String applicationSubmittedTopic;

    @Value("${kafka.topics.application-status-changed}")
    private String applicationStatusChangedTopic;

    public void sendJobCreatedEvent(JobOfferCreatedEvent event) {
        log.info("Sending job created event: {}", event);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(jobCreatedTopic, event.getJobOfferId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent job created event for job ID: {}", event.getJobOfferId());
            } else {
                log.error("Failed to send job created event for job ID: {}", event.getJobOfferId(), ex);
            }
        });
    }

    public void sendApplicationSubmittedEvent(ApplicationSubmittedEvent event) {
        log.info("Sending application submitted event: {}", event);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(applicationSubmittedTopic, event.getApplicationId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent application submitted event for application ID: {}",
                        event.getApplicationId());
            } else {
                log.error("Failed to send application submitted event for application ID: {}",
                        event.getApplicationId(), ex);
            }
        });
    }

    public void sendApplicationStatusChangedEvent(ApplicationStatusChangedEvent event) {
        log.info("Sending application status changed event: {}", event);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(applicationStatusChangedTopic, event.getApplicationId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent application status changed event for application ID: {} with new status: {}",
                        event.getApplicationId(), event.getNewStatus());
            } else {
                log.error("Failed to send application status changed event for application ID: {}",
                        event.getApplicationId(), ex);
            }
        });
    }
}