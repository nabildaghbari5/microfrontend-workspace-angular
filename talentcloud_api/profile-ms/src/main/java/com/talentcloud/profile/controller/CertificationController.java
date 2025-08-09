package com.talentcloud.profile.controller;

import com.talentcloud.profile.dto.UpdateCertificationDto;
import com.talentcloud.profile.iservice.IServiceCertification;
import com.talentcloud.profile.model.Certification;
import com.talentcloud.profile.dto.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")  // Allow requests only from the Angular app
@RestController
@RequestMapping("v1/certifications")
public class CertificationController {

    private final IServiceCertification certificationService;

    @Autowired
    public CertificationController(IServiceCertification certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping("/upload-with-data")
    public ResponseEntity<?> createCertificationWithFile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam("nom") String nom,
            @RequestParam("organisme") String organisme,
            @RequestParam("dateObtention") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateObtention,
            @RequestParam(value = "datevalidite", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datevalidite,
            @RequestParam(value = "urlVerification", required = false) String urlVerification,
            @RequestParam("file") MultipartFile file
    ) {
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can add certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to add a certification",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }
        try {
            Certification certification = certificationService.createCertificationWithFile(
                    nom,
                    organisme,
                    dateObtention,
                    datevalidite,
                    urlVerification,
                    file,
                    userId
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(certification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating certification: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }


    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadCertificationFileForCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam("file") MultipartFile file) {

        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can upload certification files",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to upload certification file",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Certification certification = certificationService.uploadCertificationFileForCurrentUser(file, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(certification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error uploading certification file: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }


    // GET: Get certification by ID for current user
    @GetMapping("/{certificationId}")
    public ResponseEntity<?> getCertificationById(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long certificationId) {

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
                            "User ID is required to view a certification",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            return certificationService.getCertificationByIdForCurrentUser(certificationId, userId)
                    .map(certification -> ResponseEntity.ok(certification))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving certification: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get all certifications for the current user
    @GetMapping("/all")
    public ResponseEntity<?> getAllCertificationsForCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can view certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to view certifications",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            List<Certification> certifications = certificationService.getAllCertificationsForCurrentUser(userId);
            return ResponseEntity.ok(certifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving certifications: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // GET: Get all certifications for the current user with pagination
    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<?> getCertificationsForCurrentUserPaginated(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable int pageNumber) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can view certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to view certifications",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            int pageSize = 5;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
            Page<Certification> certifications = certificationService.getAllCertificationsForCurrentUserPaginated(userId, pageable);
            return ResponseEntity.ok(certifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error retrieving certifications: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // POST: Add certification for the current user
    @PostMapping("/create")
    public ResponseEntity<?> addCertification(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid Certification certification) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can add certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to add a certification",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Certification savedCertification = certificationService.addCertificationForCurrentUser(certification, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCertification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating certification: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // PUT: Update certification for the current user
    @PutMapping("/{certificationId}")
    public ResponseEntity<?> updateCertification(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long certificationId,
            @RequestBody @Valid UpdateCertificationDto dto) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can update certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to update a certification",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Certification updatedCertification = certificationService.updateCertificationForCurrentUser(certificationId, dto, userId);
            return ResponseEntity.ok(updatedCertification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error updating certification: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // DELETE: Delete certification for the current user
    @DeleteMapping("/{certificationId}")
    public ResponseEntity<?> deleteCertification(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long certificationId) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can delete certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to delete a certification",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            certificationService.deleteCertificationForCurrentUser(certificationId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error deleting certification: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // Keep the original endpoints for backward compatibility

    @GetMapping("/candidate/{candidateId}/page/{pageNumber}")
    public ResponseEntity<?> getCertificationsByCandidatePaginated(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId,
            @PathVariable int pageNumber) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can view certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<Certification> certifications = certificationService.getAllCertificationsByCandidateId(candidateId, pageable);
        return ResponseEntity.ok(certifications);
    }

    @PostMapping("/create/{candidateId}")
    public ResponseEntity<?> addCertificationWithCandidateId(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long candidateId,
            @RequestBody @Valid Certification certification) {

        // Check if user has Candidate role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Candidate")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CANDIDATE role can add certifications",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        Certification savedCertification = certificationService.addCertification(certification, candidateId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCertification);
    }
}