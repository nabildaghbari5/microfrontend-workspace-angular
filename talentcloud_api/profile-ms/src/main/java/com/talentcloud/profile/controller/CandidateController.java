package com.talentcloud.profile.controller;

import com.talentcloud.profile.dto.CandidateResponse;
import com.talentcloud.profile.dto.CreateProfileDto;
import com.talentcloud.profile.dto.UpdateCandidateDto;
import com.talentcloud.profile.dto.RejectCandidateDto;
import com.talentcloud.profile.dto.ErrorResponse;
import com.talentcloud.profile.exception.CandidateNotFoundException;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.iservice.IServiceCandidate;
import com.talentcloud.profile.service.CandidateService;
import com.talentcloud.profile.service.EducationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("v1/candidates")
public class CandidateController {

    private final IServiceCandidate candidateService;
    private final EducationService educationService;

    @Autowired
    public CandidateController(IServiceCandidate candidateService, EducationService educationService) {
        this.candidateService = candidateService;
        this.educationService = educationService;
    }
    @GetMapping("/status/by-userid/{userId}")
    public ResponseEntity<?> getCandidateProfileStatusByUserId(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String userId) {

        return candidateService.getCandidateByUserId(userId)
                .map(candidate -> ResponseEntity.ok(candidate.getProfileStatus().name()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Candidate profile not found for userId: " + userId));
    }



    // Add this endpoint to your CandidateController class

    /**
     * Get candidate by userId (UUID from auth token)
     * This endpoint is used by other microservices (like job-ms) to fetch candidate details using UUID
     */
    @GetMapping("/candidate/by-userid/{userId}")
    public ResponseEntity<CandidateResponse> getCandidateByUserId(
            @PathVariable("userId") String userId,
            @RequestHeader("X-User-Roles") String userRoles) {

        try {
            CandidateResponse candidate = candidateService.findCandidateByUserId(userId);
            return ResponseEntity.ok(candidate);
        } catch (CandidateNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching candidate by userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Optional: Add a simplified version without X-User-Roles header if needed
    @GetMapping("/candidate/by-userid/{userId}/simple")
    public ResponseEntity<CandidateResponse> getCandidateByUserIdSimple(@PathVariable("userId") String userId) {
        try {
            CandidateResponse candidate = candidateService.findCandidateByUserId(userId);
            return ResponseEntity.ok(candidate);
        } catch (CandidateNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching candidate by userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Find candidate by userId
        List<Candidate> candidates = candidateService.findByUserId(userId);

        if (candidates.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "No profile found for this user. Please create a profile first.",
                    "Not Found",
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // Typically there should be only one profile per user
        Candidate candidate = candidates.get(0);

        return ResponseEntity.ok(candidate);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCompleteProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid CreateProfileDto request) {

        // Log headers for debugging
        System.out.println("X-User-Id: " + userId);
        System.out.println("X-User-Roles: " + rolesHeader);

        // Check if user has CANDIDATE role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "User does not have CANDIDATE role",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to create a candidate profile",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            // Call service to create the complete candidate profile (status will be automatically PENDING)
            Candidate savedCandidate = candidateService.createCompleteProfile(request, userId);

            // Create a success response with additional information
            Map<String, Object> response = new HashMap<>();
            response.put("candidate", savedCandidate);
            response.put("message", "Profile created successfully! Your profile is currently PENDING approval. You will receive an email notification once your profile is reviewed and approved/rejected. You cannot apply for jobs until your profile is approved.");
            response.put("status", "PENDING");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error creating candidate profile: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    // NEW ENDPOINT: Upload profile picture
    @PostMapping("/upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam("file") MultipartFile file) {

        // Check if user has CANDIDATE role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only candidates can upload profile pictures",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            String filePath = candidateService.uploadProfilePicture(userId, file);

            // Create response with file path and success message
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("filePath", filePath);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    e.getMessage(),
                    "Bad Request",
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error uploading profile picture: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // NEW ENDPOINT: Serve profile pictures
    @GetMapping("/profile-picture/{filename:.+}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename) {
        try {
            String projectRoot = System.getProperty("user.dir");
            Path filePath = Paths.get(projectRoot, "uploads", "profile-pictures", filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "application/octet-stream";
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // NEW ENDPOINT: Approve candidate profile
    @PutMapping("/{candidateId}/approve")
    public ResponseEntity<?> approveCandidate(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId) {

        // Check if user has ADMIN role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can approve candidate profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Candidate approvedCandidate = candidateService.approveCandidate(candidateId);
            return ResponseEntity.ok(approvedCandidate);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error approving candidate: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // NEW ENDPOINT: Reject candidate profile
    @PutMapping("/{candidateId}/reject")
    public ResponseEntity<?> rejectCandidate(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId,
            @RequestBody @Valid RejectCandidateDto rejectDto) {

        // Check if user has ADMIN role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can reject candidate profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Candidate rejectedCandidate = candidateService.rejectCandidate(candidateId, rejectDto.getRejectionReason());
            return ResponseEntity.ok(rejectedCandidate);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error rejecting candidate: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/candidate/{id}")
    public ResponseEntity<CandidateResponse> getCandidate(@PathVariable("id") Long candidateId) {
        return ResponseEntity.ok(candidateService.findCandidateById(candidateId));
    }

    @PutMapping("/block")
    public ResponseEntity<?> blockCandidateProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has CANDIDATE role (fixed the logic - was checking for Candidate but message said Admin)
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only candidates can block their own profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Find candidate by userId
        List<Candidate> candidates = candidateService.findByUserId(userId);

        if (candidates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "No profile found for this user",
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }

        // Typically there should be only one profile per user
        Candidate candidate = candidates.get(0);
        Long candidateId = candidate.getCandidateId();

        try {
            Candidate blockedCandidate = candidateService.blockProfile(candidateId);
            return ResponseEntity.ok(blockedCandidate);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error blocking candidate: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<?> getCandidateById(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId) {

        Optional<Candidate> optionalCandidate = candidateService.getCandidateById(candidateId);

        if (optionalCandidate.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Candidate not found with id " + candidateId,
                    "Not Found",
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // Check if user has Admin role (modified to restrict access to Admin only)
        boolean isAdmin = rolesHeader != null && Arrays.asList(rolesHeader.split(",")).contains("Admin");

        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "You don't have permission to view this profile. Admin role required.",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        Candidate candidate = optionalCandidate.get();
        return ResponseEntity.ok(candidate);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCandidates(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has ADMIN role (already restricted to Admin only)
        if (rolesHeader == null ||
                !(Arrays.asList(rolesHeader.split(",")).contains("Admin")))  {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can view all candidates",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        List<Candidate> candidates = candidateService.getAllCandidates();
        return new ResponseEntity<>(candidates, HttpStatus.OK);
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editCandidateProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid UpdateCandidateDto dto
    ){
        // Find candidate by userId
        List<Candidate> candidates = candidateService.findByUserId(userId);

        if (candidates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "No profile found for this user",
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }

        // Typically there should be only one profile per user
        Candidate candidate = candidates.get(0);
        Long candidateId = candidate.getCandidateId();

        // Check if user has CANDIDATE role or is the owner of the profile
        boolean isCandidate = rolesHeader != null && Arrays.asList(rolesHeader.split(",")).contains("Candidate");
        boolean isOwner = userId != null && userId.equals(candidate.getUserId());

        if (!isCandidate && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "You don't have permission to edit this profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Candidate updatedCandidate = candidateService.editCandidateProfile(candidateId, dto);
            return ResponseEntity.ok(updatedCandidate);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error updating candidate profile: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // New endpoint: Delete profile for candidate (self-delete)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteOwnProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has CANDIDATE role and is NOT an Admin (fixed the logic)
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only candidates can delete their own profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Find candidate by userId
        List<Candidate> candidates = candidateService.findByUserId(userId);

        if (candidates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "No profile found for this user",
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }

        // Typically there should be only one profile per user
        Candidate candidate = candidates.get(0);
        Long candidateId = candidate.getCandidateId();

        try {
            candidateService.deleteCandidate(candidateId);
            return ResponseEntity.ok()
                    .body(new ErrorResponse(
                            "Profile successfully deleted",
                            "Success",
                            LocalDateTime.now(),
                            HttpStatus.OK.value()
                    ));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error deleting profile: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // New endpoint: Admin delete candidate profile
    @DeleteMapping("/{candidateId}/delete")
    public ResponseEntity<?> adminDeleteCandidateProfile(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId) {

        // Check if user has ADMIN role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can delete candidate profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            // Check if the candidate exists
            Optional<Candidate> optionalCandidate = candidateService.getCandidateById(candidateId);
            if (optionalCandidate.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Candidate not found with id " + candidateId,
                                "Not Found",
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value()
                        ));
            }

            candidateService.deleteCandidate(candidateId);
            return ResponseEntity.ok()
                    .body(new ErrorResponse(
                            "Candidate profile successfully deleted",
                            "Success",
                            LocalDateTime.now(),
                            HttpStatus.OK.value()
                    ));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error deleting candidate profile: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}