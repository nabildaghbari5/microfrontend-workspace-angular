package com.talentcloud.profile.controller;

import com.talentcloud.profile.dto.UpdateClientDto;
import com.talentcloud.profile.dto.RejectClientDto;
import com.talentcloud.profile.exception.ClientNotFoundException;
import com.talentcloud.profile.iservice.IServiceClient;
import com.talentcloud.profile.model.Client;
import com.talentcloud.profile.dto.ErrorResponse;
import com.talentcloud.profile.repository.ClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")  // Allow requests only from the Angular app
@RestController
@RequestMapping("v1/clients")
public class ClientController {

    private final IServiceClient serviceClient;
    private final ClientRepository clientRepository;

    @Autowired
    public ClientController(IServiceClient serviceClient, ClientRepository clientRepository) {
        this.serviceClient = serviceClient;
        this.clientRepository = clientRepository;
    }
    // Add this new endpoint to your ClientController



    @GetMapping("/search")
    public ResponseEntity<?> searchApprovedClientByCompanyName(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam("companyName") String companyName) {

        // 🛡️ Role Check: Only CLIENT or CANDIDATE can use this
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> {
                    String r = role.trim().toUpperCase();
                    return r.equals("CLIENT") || r.equals("CANDIDATE");
                })) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT or CANDIDATE role can access this endpoint.",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        Optional<Client> clientOpt = serviceClient.getApprovedClientByCompanyName(companyName);

        if (clientOpt.isPresent()) {
            return ResponseEntity.ok(clientOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Client not found or not approved.",
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }
    }
    @GetMapping("/status/by-userid/{userId}")
    public ResponseEntity<?> getClientProfileStatusByUserId(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String userId) {

        return serviceClient.getClientByUserId(userId)
                .map(client -> ResponseEntity.ok(client.getProfileStatus().name()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Client profile not found for userId: " + userId));
    }


    @GetMapping("/email/by-userid/{userId}")
    public ResponseEntity<?> getClientEmailByUserId(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String userId) {

        try {
            String email = serviceClient.getClientEmailByUserId(userId);
            return ResponseEntity.ok(Map.of("email", email));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }



    @PostMapping("/create-with-logo")
    public ResponseEntity<?> createClientProfileWithLogo(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam("companyName") String companyName,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam(value = "linkedInUrl", required = false) String linkedInUrl,
            @RequestParam(value = "companyDescription", required = false) String companyDescription,
            @RequestParam(value = "logo", required = false) MultipartFile logoFile,
            HttpServletRequest request) {

        // Debug: Log all headers for troubleshooting
        System.out.println("---------- Create with Logo Headers ----------");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        System.out.println("--------------------------------------------");

        // Log specific headers for debugging
        System.out.println("X-User-Id: " + userId);
        System.out.println("X-User-Roles: " + rolesHeader);

        // Check if user has CLIENT role (case-insensitive check)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "User does not have CLIENT role",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required in the header",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        // Validate required fields
        if (companyName == null || companyName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Company name is required",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            // Create Client object from form parameters
            Client client = new Client();
            client.setCompanyName(companyName.trim());
            client.setIndustry(industry);
            client.setAddress(address);
            client.setCountry(country);
            client.setPhoneNumber(phoneNumber);
            client.setEmail(email);
            client.setWebsite(website);
            client.setLinkedInUrl(linkedInUrl);
            client.setCompanyDescription(companyDescription);

            // Create client profile first
            Client savedClient = serviceClient.createClientProfile(client, userId);

            // If logo file is provided, upload it
            if (logoFile != null && !logoFile.isEmpty()) {
                try {
                    savedClient = serviceClient.uploadLogo(savedClient.getClientId(), logoFile);
                } catch (Exception logoException) {
                    // If logo upload fails, log error but don't fail the entire creation
                    System.err.println("Logo upload failed: " + logoException.getMessage());
                    // You could optionally return a warning in the response
                }
            }

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Client profile created successfully" +
                                    (logoFile != null && !logoFile.isEmpty() ? " with logo" : ""),
                            "client", savedClient,
                            "timestamp", LocalDateTime.now()
                    ));

        } catch (IllegalStateException e) {
            // Handle case where user already has a profile
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Conflict",
                            LocalDateTime.now(),
                            HttpStatus.CONFLICT.value()
                    ));
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClientProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissionsHeader,
            @RequestBody @Valid Client client,
            HttpServletRequest request) {

        // Debug: Log all headers for troubleshooting
        System.out.println("---------- All Headers ----------");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        System.out.println("--------------------------------");

        // Log specific headers for debugging
        System.out.println("X-User-Id: " + userId);
        System.out.println("X-User-Roles: " + rolesHeader);
        System.out.println("X-User-Permissions: " + permissionsHeader);

        // Check if user has CLIENT role (case-insensitive check)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "User does not have CLIENT role",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Check if userId exists in the header
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "User ID is required in the header",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            // Important: Call the service method that accepts userId from header (status will be automatically PENDING)
            Client savedClient = serviceClient.createClientProfile(client, userId);
            return ResponseEntity.ok(savedClient);
        } catch (IllegalStateException e) {
            // Handle case where user already has a profile
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Conflict",
                            LocalDateTime.now(),
                            HttpStatus.CONFLICT.value()
                    ));
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error creating client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }
    // Alternative upload method - add this to your ClientController

    @PostMapping("/upload-logo-alt")
    public ResponseEntity<?> uploadCurrentUserLogoAlternative(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            HttpServletRequest request) {

        System.out.println("=== MULTIPART DEBUG ===");
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Method: " + request.getMethod());

        // Check if user has CLIENT role
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can upload their logo",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            // Check if request is multipart
            if (!request.getContentType().startsWith("multipart/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Request must be multipart/form-data",
                                "Bad Request",
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value()
                        ));
            }

            // Get the multipart file
            MultipartFile logoFile = null;
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                logoFile = multipartRequest.getFile("logo");

                System.out.println("Multipart files found: " + multipartRequest.getFileNames());
                System.out.println("Logo file: " + (logoFile != null ? logoFile.getOriginalFilename() : "null"));
            }

            if (logoFile == null || logoFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Please select a file to upload",
                                "Bad Request",
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value()
                        ));
            }

            Client updatedClient = serviceClient.uploadLogoByUserId(userId, logoFile);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Logo uploaded successfully",
                            "client", updatedClient,
                            "timestamp", LocalDateTime.now()
                    ));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error uploading logo: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }


    // NEW ENDPOINT: Approve client profile
    @PutMapping("/{clientId}/approve")
    public ResponseEntity<?> approveClient(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId) {

        // Check if user has ADMIN role
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can approve client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Client approvedClient = serviceClient.approveClient(clientUuid);
            return ResponseEntity.ok(approvedClient);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error approving client: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // NEW ENDPOINT: Reject client profile
    @PutMapping("/{clientId}/reject")
    public ResponseEntity<?> rejectClient(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId,
            @RequestBody @Valid RejectClientDto rejectDto) {

        // Check if user has ADMIN role
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can reject client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Client rejectedClient = serviceClient.rejectClient(clientUuid, rejectDto.getRejectionReason());
            return ResponseEntity.ok(rejectedClient);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "Error rejecting client: " + e.getMessage(),
                    "Internal Server Error",
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/view/{clientId}")
    public ResponseEntity<Client> getClientById(@PathVariable UUID clientId) {
        return clientRepository.findById(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClientById(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId) {

        // Check if user has ADMIN or CANDIDATE role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> {
                    String trimmedRole = role.trim();
                    return trimmedRole.equalsIgnoreCase("ADMIN") ||
                            trimmedRole.equalsIgnoreCase("CANDIDATE")||
                            trimmedRole.equalsIgnoreCase("CLIENT");
                })){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with ADMIN or CANDIDATE role can view client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        // Fetch client profile by ID
        Optional<Client> clientOptional = serviceClient.getClientById(clientUuid);

        if (clientOptional.isPresent()) {
            return new ResponseEntity<>(clientOptional.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Client not found with id " + clientId,
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }
    }
    @PutMapping("/{clientId}/edit")
    public ResponseEntity<?> editClientProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId,
            @RequestBody @Valid UpdateClientDto client) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can edit client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Client updatedClient = serviceClient.updateClientProfile(clientUuid, client);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error updating client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<?> getAllClientsPaginated(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable int pageNumber) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can view client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Fetch paginated client profiles
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("clientId").ascending());
        Page<Client> clients = serviceClient.getAllClients(pageable);
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{clientId}/block")
    public ResponseEntity<?> blockClientProfile(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can block client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Client blockedClient = serviceClient.blockProfile(clientUuid);
            return new ResponseEntity<>(blockedClient, HttpStatus.OK);
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error blocking client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllClients(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has ADMIN or CANDIDATE role for this operation (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> {
                    String trimmedRole = role.trim();
                    return trimmedRole.equalsIgnoreCase("ADMIN") ||
                            trimmedRole.equalsIgnoreCase("CANDIDATE");
                })) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators and candidates can view all client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        List<Client> clients = serviceClient.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentClientProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Debug log
        System.out.println("GET /profile - X-User-Id: " + userId);
        System.out.println("GET /profile - X-User-Roles: " + rolesHeader);

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can access this endpoint",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        Optional<Client> clientOptional = serviceClient.getClientByUserId(userId);

        if (clientOptional.isPresent()) {
            return new ResponseEntity<>(clientOptional.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Client profile not found for current user",
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editCurrentClientProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody @Valid UpdateClientDto client) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can edit their profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Client updatedClient = serviceClient.updateClientProfileByUserId(userId, client);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error updating client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @PutMapping("/block")
    public ResponseEntity<?> blockCurrentClientProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can block their profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Client blockedClient = serviceClient.blockProfileByUserId(userId);
            return new ResponseEntity<>(blockedClient, HttpStatus.OK);
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error blocking client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<?> deleteClientProfile(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId) {

        // Check if user has ADMIN role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can delete client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            serviceClient.deleteClientById(clientUuid);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Client profile deleted successfully",
                            "timestamp", LocalDateTime.now()
                    ));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error deleting client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCurrentClientProfile(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can delete their profile",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            serviceClient.deleteClientByUserId(userId);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Client profile deleted successfully",
                            "timestamp", LocalDateTime.now()
                    ));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error deleting client profile: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    // LOGO UPLOAD ENDPOINTS

    @PostMapping("/{clientId}/upload-logo")
    public ResponseEntity<?> uploadLogo(
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @PathVariable String clientId,
            @RequestParam(value = "logo", required = false) MultipartFile logoFile) {

        // Check if file is provided
        if (logoFile == null || logoFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Please select a file to upload",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        // Check if user has ADMIN role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only administrators can upload logos for client profiles",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        // Parse UUID safely
        UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Invalid client ID format. Must be a valid UUID.",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        try {
            Client updatedClient = serviceClient.uploadLogo(clientUuid, logoFile);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Logo uploaded successfully",
                            "client", updatedClient,
                            "timestamp", LocalDateTime.now()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error uploading logo: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadCurrentUserLogo(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam(value = "logo", required = false) MultipartFile logoFile) {

        // Check if file is provided
        if (logoFile == null || logoFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Please select a file to upload",
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        }

        // Check if user has CLIENT role (case-insensitive)
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .noneMatch(role -> role.trim().equalsIgnoreCase("CLIENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                            "Only users with CLIENT role can upload their logo",
                            "Forbidden",
                            LocalDateTime.now(),
                            HttpStatus.FORBIDDEN.value()
                    ));
        }

        try {
            Client updatedClient = serviceClient.uploadLogoByUserId(userId, logoFile);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Logo uploaded successfully",
                            "client", updatedClient,
                            "timestamp", LocalDateTime.now()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Bad Request",
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            "Not Found",
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error uploading logo: " + e.getMessage(),
                            "Internal Server Error",
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }
}