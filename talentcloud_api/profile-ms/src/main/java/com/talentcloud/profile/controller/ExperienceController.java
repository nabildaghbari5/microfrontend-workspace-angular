package com.talentcloud.profile.controller;

import com.talentcloud.profile.dto.UpdateExperienceDto;
import com.talentcloud.profile.model.Experience;
import com.talentcloud.profile.iservice.IServiceExperience;
import com.talentcloud.profile.dto.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")  // Allow requests only from the Angular app

@RestController
@RequestMapping("v1/experiences")
public class ExperienceController {

    private final IServiceExperience experienceService;

    public ExperienceController(IServiceExperience experienceService) {
        this.experienceService = experienceService;
    }

    // POST: Create experience for the current logged-in user
    @PostMapping("/create")
    public ResponseEntity<?> createExperience(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid Experience experience) {

        // Check if user has Candidate role
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
                            "User ID is required to create an experience entry",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Experience saved = experienceService.createExperienceForCurrentUser(experience, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating experience: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // PUT: Update experience
    @PutMapping("/{experienceId}/update")
    public ResponseEntity<?> updateExperience(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long experienceId,
            @RequestBody UpdateExperienceDto dto) {

        // Check if user has Candidate role
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
                            "User ID is required to update an experience entry",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Experience updated = experienceService.updateExperienceForCurrentUser(experienceId, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error updating experience: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // DELETE: Delete experience
    @DeleteMapping("/{experienceId}/delete")
    public ResponseEntity<?> deleteExperience(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long experienceId) {

        // Check if user has Candidate role
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
                            "User ID is required to delete an experience entry",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Experience deleted = experienceService.deleteExperienceForCurrentUser(experienceId, userId);
            return ResponseEntity.ok(deleted);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error deleting experience: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get experience by ID
    @GetMapping("/{experienceId}")
    public ResponseEntity<?> getExperienceById(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long experienceId) {

        // Check if user has Candidate role
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
                            "User ID is required to view an experience entry",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            return experienceService.getExperienceByIdForCurrentUser(experienceId, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving experience: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get all experiences for the current logged-in user
    @GetMapping("/all")
    public ResponseEntity<?> getAllExperiencesForCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has Candidate role
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
                            "User ID is required to view experience entries",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            List<Experience> experiences = experienceService.getAllExperiencesForCurrentUser(userId);
            return ResponseEntity.ok(experiences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving experiences: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get all experiences for the current user with pagination
    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<?> getExperiencesWithPagination(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable int pageNumber) {

        // Check if user has Candidate role
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
                            "User ID is required to view experience entries",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            int pageSize = 3; // number of experiences per page
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("dateDebut").descending());
            Page<Experience> experiences = experienceService.getExperiencesForCurrentUserWithPagination(userId, pageable);
            return ResponseEntity.ok(experiences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving paginated experiences: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }
}