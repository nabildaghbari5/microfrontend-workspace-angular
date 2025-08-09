package com.talentcloud.profile.exception;

public class CandidateNotFoundException extends RuntimeException {
    public CandidateNotFoundException(String message) {
        super(message);
    }
    public CandidateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
