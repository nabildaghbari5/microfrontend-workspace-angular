package com.talentcloud.auth.dto;

import com.talentcloud.auth.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;  // User's desired username
    private String password;
    private String firstName;
    private String lastName;// User's password
    private String email;     // User's email
    private Role role;        // Enum Role (Admin, Client, Candidate)
}
