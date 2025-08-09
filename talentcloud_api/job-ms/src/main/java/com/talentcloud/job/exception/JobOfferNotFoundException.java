package com.talentcloud.job.exception;

public class JobOfferNotFoundException extends RuntimeException {

    public JobOfferNotFoundException(String message) {
        super(message);
    }
}