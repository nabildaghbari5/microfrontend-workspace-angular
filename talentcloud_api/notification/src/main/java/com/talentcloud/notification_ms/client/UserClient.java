package com.talentcloud.notification_ms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-ms", configuration = com.talentcloud.notification_ms.config.FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/auth/v1/users/email/by-userid/{userId}")
    Map<String, String> getEmailByUserId(@PathVariable("userId") String userId);
}
