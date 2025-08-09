package com.talentcloud.job.config;

import com.talentcloud.job.dto.CandidateResponse;
import com.talentcloud.job.dto.ClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "profile-ms", configuration = FeignConfig.class)
public interface CandidateClient {

//    // Existing endpoint (keep as is)
//    @GetMapping("/v1/candidates/candidate/{id}")
//    CandidateResponse getCandidateById(@RequestHeader("X-User-Roles") String userType,
//                                       @PathVariable("id") Long candidateId);

    // New endpoint for userId (UUID) lookup - THIS IS THE ONE YOU'LL USE
    @GetMapping("/v1/candidates/candidate/by-userid/{userId}/simple")
    CandidateResponse getCandidateByUserId(@RequestHeader("X-User-Roles") String userType,
                                           @PathVariable("userId") String userId);

    @GetMapping("/v1/clients/email/by-userid/{userId}")
    Map<String, String> getClientEmailByUserId(@PathVariable("userId") String userId,
                                               @RequestHeader("X-User-Roles") String userType);

    // 🔍 GET profileStatus for a candidate
    @GetMapping("/v1/candidates/status/by-userid/{userId}")
    String getCandidateProfileStatus(@PathVariable("userId") String userId,
                                     @RequestHeader("X-User-Roles") String userType);

    // 🔍 GET profileStatus for a client
    @GetMapping("/v1/clients/status/by-userid/{userId}")
    String getClientProfileStatus(@PathVariable("userId") String userId,
                                  @RequestHeader("X-User-Roles") String userType);

    @GetMapping("/api/clients/view/{clientId}")
    ClientDto getClientById(@PathVariable("clientId") String clientId);
}
