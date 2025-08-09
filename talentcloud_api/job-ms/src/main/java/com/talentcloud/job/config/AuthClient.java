package com.talentcloud.job.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-ms", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping("/api/auth/v1/users/email/by-userid/{userId}")
    Map<String, String> getEmailByUserId(@PathVariable("userId") String userId);
}