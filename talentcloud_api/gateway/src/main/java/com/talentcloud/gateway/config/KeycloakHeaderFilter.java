package com.talentcloud.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class KeycloakHeaderFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("REQUEST PATH: " + exchange.getRequest().getPath());
        String path = exchange.getRequest().getPath().toString();
        System.out.println("REQUEST PATH: " + path);

        if (path.startsWith("/api/auth/")) {
            System.out.println("Auth endpoint detected, bypassing token check");
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .flatMap(jwt -> {
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-User-Id", jwt.getSubject())
                            .header("X-User-Roles", String.join(",", extractRoles(jwt)))  // Ensure roles are not null
                            .header("X-User-Permissions", String.join(",", extractPermissions(jwt))) // Make sure to handle null values
                            .header("X-Correlation-Id", getCorrelationId(exchange.getRequest()))
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange)); // If there's no JWT, just proceed with the chain.
    }

    // Safely extract roles from JWT
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of(); // Return an empty list if realm_access is null
        }
        // Safely retrieve the roles, if available
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles != null ? roles : List.of(); // Return empty list if roles are null
    }

    // Extract permissions if available
    private List<String> extractPermissions(Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        return permissions != null ? permissions : List.of(); // Return empty list if permissions are null
    }

    // Generate a correlation ID if not provided in request
    private String getCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
