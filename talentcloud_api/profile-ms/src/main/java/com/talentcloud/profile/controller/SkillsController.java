package com.talentcloud.profile.controller;

import com.talentcloud.profile.iservice.IServiceSkills;
import com.talentcloud.profile.model.Skills;
import com.talentcloud.profile.dto.UpdateSkillsDto;
import com.talentcloud.profile.dto.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")  // Allow requests only from the Angular app

@RestController
@RequestMapping("v1/skills")
public class SkillsController {

    private final IServiceSkills skillsService;

    @Autowired
    public SkillsController(IServiceSkills skillsService) {
        this.skillsService = skillsService;
    }

    // GET: Get all skills for current user
    @GetMapping("/current")
    public ResponseEntity<?> getSkillsForCurrentUser(
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
                            "User ID is required to view skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            List<Skills> skills = skillsService.getSkillsForCurrentUser(userId);
            if (skills.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get the most recent skills for current user
    @GetMapping("/current/latest")
    public ResponseEntity<?> getMostRecentSkillsForCurrentUser(
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
                            "User ID is required to view skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Optional<Skills> skills = skillsService.getMostRecentSkillsForCurrentUser(userId);
            return skills.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get all skills for current user with pagination
    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<?> getAllSkillsForCurrentUserPaginated(
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
                            "User ID is required to view skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            int pageSize = 5;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
            Page<Skills> skillsPage = skillsService.getAllSkillsForCurrentUserPaginated(userId, pageable);
            return ResponseEntity.ok(skillsPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get skills by ID for current user
    @GetMapping("/{skillsId}")
    public ResponseEntity<?> getSkillsById(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long skillsId) {

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
                            "User ID is required to view skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            return skillsService.getSkillsByIdForCurrentUser(skillsId, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // POST: Add skills for current user
    @PostMapping("/create")
    public ResponseEntity<?> addSkillsForCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid Skills skills) {

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
                            "User ID is required to add skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Skills savedSkills = skillsService.addSkillsForCurrentUser(skills, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSkills);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // PUT: Update skills for current user
    @PutMapping("/{skillsId}")
    public ResponseEntity<?> updateSkills(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long skillsId,
            @RequestBody @Valid UpdateSkillsDto dto) {

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
                            "User ID is required to update skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Skills updatedSkills = skillsService.updateSkillsForCurrentUser(skillsId, dto, userId);
            return ResponseEntity.ok(updatedSkills);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error updating skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // DELETE: Delete skills for current user
    @DeleteMapping("/{skillsId}")
    public ResponseEntity<?> deleteSkills(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long skillsId) {

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
                            "User ID is required to delete skills",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            skillsService.deleteSkillsForCurrentUser(skillsId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error deleting skills: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // Keep the original endpoints for backward compatibility

    @GetMapping("/candidate/{candidateId}/page/{pageNumber}")
    public ResponseEntity<?> getAllSkillsByCandidatePaginated(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId,
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

        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        Page<Skills> skillsPage = skillsService.getAllSkillsByCandidateId(candidateId, pageable);
        return ResponseEntity.ok(skillsPage);
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getSkillsByCandidateId(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId) {

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

        List<Skills> skills = skillsService.getSkillsByCandidateId(candidateId);
        if (skills.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/create/{candidateId}")
    public ResponseEntity<?> addSkills(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId,
            @RequestBody @Valid Skills skills) {

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

        Skills savedSkills = skillsService.addSkills(skills, candidateId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSkills);
    }
}