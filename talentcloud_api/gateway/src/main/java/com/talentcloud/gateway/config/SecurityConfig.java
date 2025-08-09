package com.talentcloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.disable()) // Temporairement pour tester
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/auth/**",
                                "/api/auth/{id}",
                                "/v3/api-docs/**",
                                "/api/v1/**",
                                "/api/jobs/**",
                                "/api/notification/**",
                                "/api/v1/certifications/**",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/uploads/**", // Déplacé ici avec pathMatchers
                                "/api/public/**"

                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((exchange, ex) ->
                                Mono.fromRunnable(() ->
                                        exchange.getResponse().setStatusCode(
                                                org.springframework.http.HttpStatus.UNAUTHORIZED
                                        )
                                )
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation("http://localhost:8180/realms/talent");
    }

    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) return Collections.emptyList();

            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }
}