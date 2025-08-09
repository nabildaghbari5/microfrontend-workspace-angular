//package com.talentcloud.notification_ms.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@FeignClient(name = "profile-ms", configuration = com.talentcloud.notification_ms.config.FeignConfig.class)
//public interface ProfileClient {
//
//    @GetMapping("/v1/clients/email/by-userid/{userId}")
//    Map<String, String> getClientEmailByUserId(
//            @PathVariable("userId") String userId,
//            @RequestHeader("X-User-Roles") String userType
//    );
//}
