package com.talentcloud.auth.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakService {
    private final RestTemplate restTemplate;
    private final String keycloakTokenUrl = "http://localhost:8180/realms/talent/protocol/openid-connect/token";
    private final String clientId = "public-client";
    private final String clientSecret = "wRoBJC5HMuPpxRopySCdxYrzFcADc4s6";
    private final String realmAdminUrl = "http://localhost:8180/admin/realms/talent/users";
    private final String realmRolesUrl = "http://localhost:8180/admin/realms/talent/roles";

    public KeycloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "client_credentials");
        body.add("scope", "openid"); // Add scope parameter

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(keycloakTokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody().get("access_token").toString();
            } else {
                throw new RuntimeException("Failed to get admin token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while getting admin token", e);
        }
    }

    public ResponseEntity<String> fetchUsersFromKeycloak() {
        String adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(realmAdminUrl, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> fetchRolesFromKeycloak() {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(realmRolesUrl, HttpMethod.GET, entity, String.class);
    }
}