package com.talentcloud.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Allow CORS for /api endpoints
                .allowedOrigins("http://localhost:5173")  // Allow requests from Angular app
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allow necessary methods
                .allowedHeaders("Authorization", "Content-Type", "X-User-Id", "X-User-Roles")  // Allow headers
                .allowCredentials(true);  // Allow credentials (cookies, etc.)
    }
}

