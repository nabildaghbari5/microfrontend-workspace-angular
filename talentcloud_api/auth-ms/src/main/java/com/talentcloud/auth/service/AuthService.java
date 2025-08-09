package com.talentcloud.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.talentcloud.auth.dto.LoginRequest;
import com.talentcloud.auth.dto.LoginResponse;
import com.talentcloud.auth.dto.RegisterRequest;
import com.talentcloud.auth.dto.UserResponse;
import com.talentcloud.auth.model.User;
import com.talentcloud.auth.model.Role;
import com.talentcloud.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final String KEYCLOAK_ADMIN_URL = "/admin/realms/talent/users";
    private final String KEYCLOAK_ROLES_URL = "/admin/realms/talent/roles";
    private final KeycloakService keycloakService;
    private final WebClient webClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuthService(KeycloakService keycloakService, WebClient.Builder webClientBuilder,
                       UserRepository userRepository, ObjectMapper objectMapper) {
        this.keycloakService = keycloakService;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8180").build();
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Mono<UserResponse> getUserById(UUID userId) {
        logger.info("Fetching user with ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with ID: " + userId)))
                .map(UserResponse::fromUser)
                .doOnSuccess(user -> logger.info("Successfully retrieved user: {}", user.getUsername()))
                .doOnError(error -> logger.error("Error retrieving user with ID {}: {}", userId, error.getMessage()));
    }
    public Mono<String> getEmailByUserId(String userId) {
        return userRepository.findByKeycloakId(userId)
                .map(User::getEmail)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with ID: " + userId)));
    }



    public Mono<LoginResponse> login(LoginRequest request) {
        logger.info("🚀 Attempting login for user: {}", request.getUsername());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", "public-client");
        formData.add("client_secret", "wRoBJC5HMuPpxRopySCdxYrzFcADc4s6");
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());

        logger.info("📦 Sending login request to Keycloak with form-data:\n{}", formData);

        return webClient.post()
                .uri("/realms/talent/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchangeToMono(response -> {
                    logger.info("🔎 Login HTTP Status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> {
                                // Log the response body to see what's returned
                                logger.info("📩 Login HTTP Response Body: {}", body);
                            })
                            .flatMap(responseBody -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    try {
                                        JsonNode tokenJson = objectMapper.readTree(responseBody);
                                        String accessToken = tokenJson.get("access_token") != null ?
                                                tokenJson.get("access_token").asText() : null;

                                        if (accessToken != null) {
                                            logger.info("✅ Login successful for user: {}", request.getUsername());

                                            // Decode JWT to get user info
                                            try {
                                                // Decode the JWT token to get claims
                                                String[] chunks = accessToken.split("\\.");
                                                Base64.Decoder decoder = Base64.getUrlDecoder();
                                                String payload = new String(decoder.decode(chunks[1]));
                                                JsonNode claims = objectMapper.readTree(payload);

                                                String username = claims.has("preferred_username") ?
                                                        claims.get("preferred_username").asText() : request.getUsername();
                                                String email = claims.has("email") ?
                                                        claims.get("email").asText() : null;
                                                String userId = claims.has("sub") ?
                                                        claims.get("sub").asText() : null;

                                                // Try to find user in database
                                                return userRepository.findByUsername(username)
                                                        .switchIfEmpty(
                                                                // If user not found, try to find by Keycloak ID
                                                                userRepository.findByKeycloakId(userId)
                                                                        .switchIfEmpty(
                                                                                // If still not found, create a new user
                                                                                Mono.defer(() -> {
                                                                                    logger.info("Creating new user record for: {}", username);

                                                                                    User newUser = new User();
                                                                                    newUser.generateId();
                                                                                    newUser.setUsername(username);
                                                                                    newUser.setEmail(email);
                                                                                    newUser.setKeycloakId(userId);
                                                                                    // Set default role if needed

                                                                                    return userRepository.save(newUser);
                                                                                })
                                                                        )
                                                        )
                                                        .map(user -> {
                                                            UserResponse userResponse = UserResponse.fromUser(user);
                                                            return new LoginResponse(accessToken, userResponse);
                                                        });
                                            } catch (Exception e) {
                                                logger.error("Error processing JWT token", e);
                                                // Even if we can't process the token properly, return basic info
                                                UserResponse basicUser = new UserResponse();
                                                basicUser.setUsername(request.getUsername());
                                                return Mono.just(new LoginResponse(accessToken, basicUser));
                                            }
                                        } else {
                                            logger.error("❗ Access token missing in response");
                                            return Mono.error(new RuntimeException("Access token missing"));
                                        }
                                    } catch (Exception e) {
                                        logger.error("❗ Error parsing token response", e);
                                        return Mono.error(new RuntimeException("Login failed while parsing token: " + e.getMessage()));
                                    }
                                } else {
                                    logger.error("❗ Login failed with response: {}", responseBody);
                                    return Mono.error(new RuntimeException("Invalid client credentials or other error: " + responseBody));
                                }
                            });
                })
                .onErrorResume(e -> {
                    logger.error("🔥 Login failed", e);
                    return Mono.error(new RuntimeException("Login failed: " + e.getMessage()));
                });
    }

    private String formatJsonIfPossible(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObj = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        } catch (Exception e) {
            // If it's not JSON, just return as-is
            return json;
        }
    }


    public Mono<String> register(RegisterRequest request) {
        logger.info("Starting user registration process for username: {}", request.getUsername());

        return userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        logger.warn("User already exists in database: {}", request.getUsername());
                        return Mono.error(new RuntimeException("User already exists!"));
                    }
                    return Mono.fromCallable(() -> keycloakService.getAdminToken());
                })
                .flatMap(adminToken -> {
                    logger.info("Obtained admin token from Keycloak");

                    // Adding firstName and lastName to the keycloakUser
                    Map<String, Object> keycloakUser = new HashMap<>();
                    keycloakUser.put("username", request.getUsername());
                    keycloakUser.put("email", request.getEmail());
                    keycloakUser.put("enabled", true);

                    // Add first name and last name
                    keycloakUser.put("firstName", request.getFirstName());
                    keycloakUser.put("lastName", request.getLastName());

                    Map<String, Object> credentials = new HashMap<>();
                    credentials.put("type", "password");
                    credentials.put("value", request.getPassword());
                    credentials.put("temporary", false);
                    keycloakUser.put("credentials", Collections.singletonList(credentials));

                    logger.info("Sending user creation request to Keycloak with user data: {}", keycloakUser);

                    return webClient.post()
                            .uri(KEYCLOAK_ADMIN_URL)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(keycloakUser)
                            .exchangeToMono(response -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    String location = response.headers().asHttpHeaders().getFirst("Location");
                                    logger.info("User created with Location: {}", location);
                                    if (location != null) {
                                        String userId = location.substring(location.lastIndexOf("/") + 1);
                                        return Mono.just(new KeycloakUserInfo(userId, adminToken));
                                    } else {
                                        return Mono.error(new RuntimeException("Location header missing in Keycloak create response"));
                                    }
                                } else {
                                    return response.bodyToMono(String.class)
                                            .flatMap(errorBody -> {
                                                logger.error("Error creating user in Keycloak: {}", errorBody);
                                                return Mono.error(new RuntimeException("Failed to create user in Keycloak"));
                                            });
                                }
                            });
                })
                .flatMap(userInfo -> {
                    return webClient.get()
                            .uri(KEYCLOAK_ROLES_URL)
                            .header("Authorization", "Bearer " + userInfo.adminToken)
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(rolesResponse -> {
                                String roleName = request.getRole().name();
                                String roleId = findRoleId(rolesResponse, roleName);
                                if (roleId == null) {
                                    return Mono.error(new RuntimeException("Requested role not found in Keycloak: " + roleName));
                                }
                                logger.info("Found role ID: {}", roleId);
                                return Mono.just(new KeycloakRoleInfo(userInfo.userId, userInfo.adminToken, roleId, roleName));
                            });
                })
                .flatMap(roleInfo -> {
                    String roleAssignmentUrl = KEYCLOAK_ADMIN_URL + "/" + roleInfo.userId + "/role-mappings/realm";
                    List<Map<String, Object>> rolesList = new ArrayList<>();
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", roleInfo.roleId);
                    roleMap.put("name", roleInfo.roleName);
                    rolesList.add(roleMap);

                    return webClient.post()
                            .uri(roleAssignmentUrl)
                            .header("Authorization", "Bearer " + roleInfo.adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(rolesList)
                            .retrieve()
                            .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                                    clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                        logger.error("Error assigning role in Keycloak: {}", errorBody);
                                        return Mono.error(new RuntimeException("Failed to assign role"));
                                    })
                            )
                            .bodyToMono(String.class)
                            .thenReturn(roleInfo);
                })
                .flatMap(roleInfo -> {
                    User user = new User();
                    user.generateId();
                    user.setUsername(request.getUsername());
                    user.setEmail(request.getEmail());
                    user.setRole(Role.valueOf(roleInfo.roleName));
                    user.setKeycloakId(roleInfo.userId);
                    user.setUserId(roleInfo.userId);
                    logger.info("User creation details -> id: {}, username: {}, email: {}, role: {}, keycloakId: {}, createdAt: {}",
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole(),
                            user.getKeycloakId(),
                            user.getCreatedAt() != null ? user.getCreatedAt().toString() : "NULL"
                    );

                    return userRepository.save(user)
                            .doOnSuccess(savedUser -> logger.info("User saved successfully with ID: {}", savedUser.getId()))
                            .onErrorResume(e -> {
                                logger.error("Database save failed: {}", e.getMessage(), e);
                                return Mono.error(new RuntimeException("Database save failed: " + e.getMessage()));
                            });
                })
                .map(savedUser -> {
                    logger.info("User registered and role assigned successfully!");
                    return "User registered and role assigned successfully!";
                })
                .onErrorResume(e -> {
                    logger.error("Registration failed", e);
                    return Mono.error(new RuntimeException("Registration failed: " + e.getMessage()));
                });
    }

    // Helper class to pass information between reactive steps
    private static class KeycloakUserInfo {
        private final String userId;
        private final String adminToken;

        public KeycloakUserInfo(String userId, String adminToken) {
            this.userId = userId;
            this.adminToken = adminToken;
        }
    }

    // Helper class to pass role information
    private static class KeycloakRoleInfo {
        private final String userId;
        private final String adminToken;
        private final String roleId;
        private final String roleName;

        public KeycloakRoleInfo(String userId, String adminToken, String roleId, String roleName) {
            this.userId = userId;
            this.adminToken = adminToken;
            this.roleId = roleId;
            this.roleName = roleName;
        }
    }

    // Helper method to extract user ID
    private String extractUserIdFromResponse(String responseBody) {
        try {
            JsonNode userArray = objectMapper.readTree(responseBody);
            if (userArray.isArray() && userArray.size() > 0) {
                return userArray.get(0).get("id").asText();
            }
        } catch (Exception e) {
            logger.error("Error extracting user ID from response", e);
        }
        return null;
    }

    // Helper method to find role ID
    private String findRoleId(String rolesResponseBody, String roleName) {
        try {
            JsonNode rolesArray = objectMapper.readTree(rolesResponseBody);
            for (JsonNode role : rolesArray) {
                if (role.has("name") && role.get("name").asText().equals(roleName)) {
                    return role.get("id").asText();
                }
            }
        } catch (Exception e) {
            logger.error("Error finding role ID", e);
        }
        return null;
    }

    public Mono<String> sendForgotPasswordEmail(String email) {
        String adminToken = keycloakService.getAdminToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/talent/users")
                        .queryParam("email", email)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(body);
                        if (jsonNode.isArray() && jsonNode.size() > 0) {
                            String userId = jsonNode.get(0).get("id").asText();

                            return webClient.put()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/admin/realms/talent/users/" + userId + "/execute-actions-email")
                                            .queryParam("client_id", "public-client")
                                            .queryParam("redirect_uri", "http://localhost:5173/reset-password")
                                            .build())
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .bodyValue(List.of("UPDATE_PASSWORD"))
                                    .retrieve()
                                    .onStatus(status -> status.isError(),
                                            clientResponse -> clientResponse.bodyToMono(String.class).flatMap(error -> {
                                                logger.error("💥 Keycloak responded with error: {}", error);
                                                return Mono.error(new RuntimeException("Keycloak error: " + error));
                                            }))
                                    .toBodilessEntity()
                                    .thenReturn("📨 Password reset email has been triggered.");
                        } else {
                            return Mono.error(new RuntimeException("❌ User not found with email: " + email));
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing user lookup response", e);
                        return Mono.error(new RuntimeException("Internal error parsing Keycloak response"));
                    }
                });
    }
}