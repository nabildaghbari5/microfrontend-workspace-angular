package com.talentcloud.profile.client;

import com.talentcloud.profile.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// FIXED: Add the correct path prefix and URL
@FeignClient(
        name = "auth-ms",
        url = "${auth-ms.url:http://localhost:8222}",

        configuration = FeignConfig.class
)
public interface UserClient {
    // FIXED: Use the correct endpoint path that works in Postman
    @GetMapping("/api/auth/v1/users/email/by-userid/{userId}")
    Map<String, String> getUserEmail(@PathVariable("userId") String userId);
}