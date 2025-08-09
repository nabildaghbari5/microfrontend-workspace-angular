package com.talentcloud.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableR2dbcAuditing
public class R2dbcAuditingConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
