package com.talentcloud.profile.model;

public enum Gender {
    MEN("Men"),
    WOMEN("Women"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}