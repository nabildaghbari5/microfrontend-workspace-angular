package com.talentcloud.auth.dto;

import com.talentcloud.auth.model.Role;
import com.talentcloud.auth.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private String id;        // UUID from our database
    private String userId;    // User ID string which can be sent to other services
    private String keycloakId; // Keycloak's user ID
    private String username;
    private String email;
    private String role;

    // Static factory method to create UserResponse from User entity
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId().toString());
        response.setUserId(user.getUserId());
        response.setKeycloakId(user.getKeycloakId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }
}