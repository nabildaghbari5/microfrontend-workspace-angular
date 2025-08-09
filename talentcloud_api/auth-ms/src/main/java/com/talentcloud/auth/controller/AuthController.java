package com.talentcloud.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentcloud.auth.dto.LoginRequest;
import com.talentcloud.auth.dto.LoginResponse;
import com.talentcloud.auth.dto.RegisterRequest;
import com.talentcloud.auth.dto.UserResponse;
import com.talentcloud.auth.model.User;
import com.talentcloud.auth.repository.UserRepository;
import com.talentcloud.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuthController(AuthService authService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/forgot-password")
    public Mono<String> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        return authService.sendForgotPasswordEmail(email);
    }

    @GetMapping("/v1/users/email/by-userid/{userId}")
    public Mono<Map<String, String>> getEmailByUserId(@PathVariable String userId) {
        logger.info("📥 Reçu une requête pour userId: {}", userId);
        return authService.getEmailByUserId(userId)
                .doOnNext(email -> logger.info("✅ Email trouvé: {}", email))
                .map(email -> Map.of("email", email))
                .onErrorResume(e -> {
                    logger.error("❌ Erreur lors de la récupération de l'email pour userId {}: {}", userId, e.getMessage());
                    return Mono.just(Map.of("error", e.getMessage()));
                });
    }


    @GetMapping("/debug-token")
    public Mono<String> debugToken(ServerWebExchange exchange) {
        logger.info("Received request to debug-token endpoint");

        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .map(authHeader -> {
                    String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

                    try {
                        // Log token parts
                        String[] parts = token.split("\\.");
                        logger.info("Token parts: {}", parts.length);

                        if (parts.length >= 2) {
                            // Decode header
                            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                            logger.info("Token header: {}", header);

                            // Decode payload
                            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                            logger.info("Token payload: {}", payload);

                            return "Token debug information logged. Parts: " + parts.length;
                        } else {
                            return "Invalid token format, not enough parts";
                        }
                    } catch (Exception e) {
                        logger.error("Error debugging token", e);
                        return "Error debugging token: " + e.getMessage();
                    }
                })
                .switchIfEmpty(Mono.just("No authorization header found"));
    }


    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request); // Returns a JWT token for authenticated requests
    }

    @PostMapping("/register")
    public Mono<String> register(@RequestBody RegisterRequest request) {
        return authService.register(request); // No authentication required, open to all
    }
    @GetMapping("/me")
    public Mono<UserResponse> me(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String[] chunks = token.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                JsonNode claims = objectMapper.readTree(payload);
                String userId = claims.get("sub").asText();

                return userRepository.findByKeycloakId(userId)
                        .map(UserResponse::fromUser);
            } catch (Exception e) {
                return Mono.error(new RuntimeException("Invalid token"));
            }
        }
        return Mono.error(new RuntimeException("Missing authorization header"));
    }

}