package com.talentcloud.job.controller;

import com.talentcloud.job.dto.CreateJobOfferDto;
import com.talentcloud.job.dto.ErrorResponse;
import com.talentcloud.job.dto.JobOfferClientView;
import com.talentcloud.job.dto.UpdateJobOfferDto;
import com.talentcloud.job.model.JobOffer;
import com.talentcloud.job.iservice.IServiceJobOffer;
import com.talentcloud.job.service.JobOfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("v1/job-offers")
public class JobOfferController {

    private final IServiceJobOffer jobOfferService;

    @Autowired
    public JobOfferController(IServiceJobOffer jobOfferService) {
        this.jobOfferService = jobOfferService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createJobOffer(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid CreateJobOfferDto jobOfferDto) {

        System.out.println("Create - X-User-Id: " + userId);
        System.out.println("Create - X-User-Roles: " + rolesHeader);

        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "User does not have CLIENT role",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required to create a job offer",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            JobOffer savedJobOffer = jobOfferService.createJobOffer(jobOfferDto, userId);
            return ResponseEntity.ok(savedJobOffer);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Profile Not Approved",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating job offer: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }


    @GetMapping("/{jobOfferId}")
    public ResponseEntity<?> getJobOfferById(@PathVariable Long jobOfferId) {

        System.out.println("GetById - jobOfferId: " + jobOfferId);

        Optional<JobOffer> optionalJobOffer = jobOfferService.getJobOfferById(jobOfferId);

        if (optionalJobOffer.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Job offer not found with id " + jobOfferId,
                    "Not Found",
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        JobOffer jobOffer = optionalJobOffer.get();

        // Public endpoint - anyone can view any job offer
        return ResponseEntity.ok(jobOffer);
    }

    // Modify the client endpoint to make it more robust
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getJobOffersByClientId(@PathVariable String clientId) {

        System.out.println("GetByClientId - clientId: " + clientId);

        // Public endpoint - anyone can view any client's job offers
        List<JobOffer> jobOffers = jobOfferService.getJobOffersByClientId(clientId);
        return ResponseEntity.ok(jobOffers);
    }
    @DeleteMapping("/{jobOfferId}")
    public ResponseEntity<?> deleteJobOffer(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long jobOfferId) {

        System.out.println("Delete - X-User-Id: " + userId);
        System.out.println("Delete - X-User-Roles: " + rolesHeader);
        System.out.println("Delete - jobOfferId: " + jobOfferId);

        Optional<JobOffer> optionalJobOffer = jobOfferService.getJobOfferById(jobOfferId);

        if (optionalJobOffer.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Job offer not found with id " + jobOfferId,
                    "Not Found",
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        JobOffer jobOffer = optionalJobOffer.get();

        // Check if user has CLIENT role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only clients can delete job offers",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if the user is the owner using String comparison
        if (userId == null || !userId.equals(jobOffer.getClientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "You can only delete your own job offers",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            jobOfferService.deleteJobOffer(jobOfferId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error deleting job offer: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{jobOfferId}")
    public ResponseEntity<?> updateJobOffer(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable Long jobOfferId,
            @RequestBody @Valid UpdateJobOfferDto dto) {

        System.out.println("Update - X-User-Id: " + userId);
        System.out.println("Update - X-User-Roles: " + rolesHeader);
        System.out.println("Update - jobOfferId: " + jobOfferId);

        Optional<JobOffer> optionalJobOffer = jobOfferService.getJobOfferById(jobOfferId);

        if (optionalJobOffer.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Job offer not found with id " + jobOfferId,
                    "Not Found",
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        JobOffer jobOffer = optionalJobOffer.get();

        // Check if user has CLIENT role
        if (rolesHeader == null || !Arrays.asList(rolesHeader.split(",")).contains("Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only clients can update job offers",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if the user is the owner using String comparison
        if (userId == null || !userId.equals(jobOffer.getClientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "You can only update your own job offers",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            JobOffer updatedJobOffer = jobOfferService.updateJobOffer(jobOfferId, dto);
            return ResponseEntity.ok(updatedJobOffer);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error updating job offer: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllJobOffers(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        System.out.println("GetAll - X-User-Roles: " + rolesHeader);

        // All roles can view all job offers
        List<JobOffer> jobOffers = jobOfferService.getAllJobOffers();
        return ResponseEntity.ok(jobOffers);
    }

//    @GetMapping("/views")
//    public ResponseEntity<List<JobOfferClientView>> getAllViews() {
//        List<JobOfferClientView> views = jobOfferService.getAllJobOfferViews();
//        return ResponseEntity.ok(views);
//    }

}