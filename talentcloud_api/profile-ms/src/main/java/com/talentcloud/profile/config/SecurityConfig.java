//package com.talentcloud.profile.config;
//
//import com.talentcloud.profile.model.Role;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//
//import java.security.KeyFactory;
//import java.security.NoSuchAlgorithmException;
//import java.security.interfaces.RSAPublicKey;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//
//@Configuration
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfig {
//
//   // @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/v3/api-docs.yaml").permitAll()
//                        .requestMatchers("/actuator/**").permitAll()
//                        .requestMatchers("/api/profile/**")
//                        .hasAnyAuthority(Role.ROLE_ADMIN.getRole(), Role.ROLE_CLIENT.getRole(),Role.ROLE_CANDIDATE.getRole())
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> {
//                                jwt.decoder(jwtDecoder());
//                        }));
//
//        return http.build();
//    }
//
//    @Bean
//    JwtDecoder jwtDecoder() {
//        return JwtDecoders.fromIssuerLocation("http://localhost:8080/realms/talentcloud");
//    }
//}
//
