package com.talentcloud.profile.model;

public enum Role {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_CLIENT("ROLE_CLIENT"),
    ROLE_CANDIDATE("ROLE_CANDIDATE");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
