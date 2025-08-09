package com.talentcloud.profile.service;

import com.talentcloud.profile.client.UserClient;
import com.talentcloud.profile.dto.UpdateClientDto;
import com.talentcloud.profile.dto.event.ClientProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileStatusChangedEvent;
import com.talentcloud.profile.exception.ClientNotFoundException;
import com.talentcloud.profile.iservice.IServiceClient;
import com.talentcloud.profile.kafka.EventPublisherService;
import com.talentcloud.profile.model.Client;
import com.talentcloud.profile.model.ProfileStatus;
import com.talentcloud.profile.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientService implements IServiceClient {

    private final ClientRepository clientRepository;
    private final String uploadDirectory;
    private final EventPublisherService eventPublisherService;
    private UserClient userClient;

    @Autowired
    public ClientService(ClientRepository clientRepository, EventPublisherService eventPublisherService, UserClient userClient) {
        this.clientRepository = clientRepository;
        this.eventPublisherService = eventPublisherService;
        // Create upload directory using system property
        this.uploadDirectory = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "logos";
        createUploadDirectory();
        this.userClient = userClient;
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    public String getClientEmailByUserId(String userId) {
        return clientRepository.findByUserId(userId).stream()
                .findFirst()
                .map(Client::getEmail)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with userId " + userId));
    }

    @Override
    public Client createClientProfile(Client client) {
        // This method should not be called directly
        // It's kept for backward compatibility
        if (client.getUserId() == null || client.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        client.setCreatedAt(LocalDateTime.now());
        // Set default status to PENDING when profile is created
        client.setProfileStatus(ProfileStatus.PENDING);
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client createClientProfile(Client client, String userId) {
        // Vérifier si un profil client existe déjà
        List<Client> existingClients = clientRepository.findByUserId(userId);
        if (!existingClients.isEmpty()) {
            throw new IllegalStateException("User already has a client profile");
        }

        // Associer l'utilisateur au client
        client.setUserId(userId);
        client.setProfileStatus(ProfileStatus.PENDING);
        client.setCreatedAt(LocalDateTime.now());

        // Sauvegarder et forcer la génération de clientId
        Client savedClient = clientRepository.saveAndFlush(client);

        // 📨 Publier l’événement avec récupération d’email depuis auth-ms
        try {
            String userEmail;

            // 🔍 Tenter de récupérer l'email via auth-ms (userClient)
            try {
                log.info("🔍 Attempting to fetch email for userId: {}", userId);
                Map<String, String> userInfo = userClient.getUserEmail(userId);
                log.info("📥 Response from auth-ms: {}", userInfo);
                userEmail = userInfo.get("email");

                if (userEmail == null || userEmail.trim().isEmpty()) {
                    log.warn("⚠️ Auth-ms returned null/empty email for userId: {}", userId);
                    userEmail = generateFallbackEmail(userId);
                } else {
                    log.info("✅ Successfully fetched email: {} for userId: {}", userEmail, userId);
                }

            } catch (feign.FeignException.InternalServerError e) {
                log.error("🔥 Auth-ms returned 500 Internal Server Error for userId: {} - using fallback email", userId);
                userEmail = generateFallbackEmail(userId);
            } catch (feign.FeignException.NotFound e) {
                log.error("❌ User not found in auth-ms for userId: {} - using fallback email", userId);
                userEmail = generateFallbackEmail(userId);
            } catch (feign.FeignException.ServiceUnavailable e) {
                log.error("🚫 Auth-ms service unavailable for userId: {} - using fallback email", userId);
                userEmail = generateFallbackEmail(userId);
            } catch (feign.FeignException e) {
                log.error("🌐 Feign exception when calling auth-ms for userId: {} - using fallback email. Status: {}", userId, e.status());
                userEmail = generateFallbackEmail(userId);
            } catch (Exception e) {
                log.error("⚠️ Unexpected error fetching email for userId: {} - using fallback email. Error: {}", userId, e.getMessage());
                userEmail = generateFallbackEmail(userId);
            }

            // Créer et envoyer l’événement
            ClientProfileCreatedEvent event = ClientProfileCreatedEvent.builder()
                    .userId(userId)
                    .email(userEmail)
                //    .firstName(savedClient.getFirstName())
                  //  .lastName(savedClient.getLastName())
                    .profileType("CLIENT")
                    .status("PENDING")
                    .createdAt(savedClient.getCreatedAt())
                    .clientId(savedClient.getClientId().toString())
                    .build();

            eventPublisherService.publishClientProfileCreatedEvent(event);
            log.info("✅ ClientProfileCreatedEvent sent for client: {} with email: {}", userId, userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send ClientProfileCreatedEvent for client: {}", userId, e);
            log.info("🔄 Profile creation continues despite event failure for userId: {}", userId);
        }

        return savedClient;
    }
    private String generateFallbackEmail(String userId) {
        // Generate a meaningful fallback email that your notification service can handle
        String shortUserId = userId.length() > 8 ? userId.substring(0, 8) : userId;
        return "pending-user-" + shortUserId + "@talentcloud.com";
    }



    @Override
    @Transactional
    public Client approveClient(UUID clientId) throws Exception {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + clientId));

        ProfileStatus oldStatus = client.getProfileStatus();

        client.setProfileStatus(ProfileStatus.APPROVED);
        client.setRejectionReason(null);
        client.setUpdatedAt(LocalDateTime.now());

        Client updatedClient = clientRepository.save(client);

        if (oldStatus != ProfileStatus.APPROVED) {
            try {
                String email = getEmailFromAuth(updatedClient.getUserId());

                ProfileStatusChangedEvent event = ProfileStatusChangedEvent.builder()
                        .userId(updatedClient.getUserId())
                        .userEmail(email)
                        .userType("CLIENT")
                        .profileStatus("APPROVED")
                        .message("Félicitations ! Votre profil client a été approuvé.")
                        .build();

                eventPublisherService.publishProfileStatusChangedEvent(event);
            } catch (Exception e) {
                log.error("❌ Failed to publish ProfileStatusChangedEvent for client userId: {}", updatedClient.getUserId(), e);
            }
        }

        return updatedClient;
    }


    @Override
    @Transactional
    public Client rejectClient(UUID clientId, String rejectionReason) throws Exception {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + clientId));

        ProfileStatus oldStatus = client.getProfileStatus();

        client.setProfileStatus(ProfileStatus.REJECTED);
        client.setRejectionReason(rejectionReason);
        client.setUpdatedAt(LocalDateTime.now());

        Client updatedClient = clientRepository.save(client);

        if (oldStatus != ProfileStatus.REJECTED) {
            try {
                String email = getEmailFromAuth(updatedClient.getUserId());

                ProfileStatusChangedEvent event = ProfileStatusChangedEvent.builder()
                        .userId(updatedClient.getUserId())
                        .userEmail(email)
                        .userType("CLIENT")
                        .profileStatus("REJECTED")
                        .message("Votre profil client a été rejeté. Raison: " + rejectionReason)
                        .build();

                eventPublisherService.publishProfileStatusChangedEvent(event);
            } catch (Exception e) {
                log.error("❌ Failed to publish ProfileStatusChangedEvent for client userId: {}", updatedClient.getUserId(), e);
            }
        }

        return updatedClient;
    }


    @Override
    public Optional<Client> getClientById(UUID clientId) {
        return clientRepository.findById(clientId);
    }

    @Override
    @Transactional
    public Client updateClientProfile(UUID clientId, UpdateClientDto client) {
        return clientRepository.findById(clientId)
                .map(existingClient -> {
                    if (client.getCompanyName() != null) existingClient.setCompanyName(client.getCompanyName());
                    if (client.getIndustry() != null) existingClient.setIndustry(client.getIndustry());
                    if (client.getAddress() != null) existingClient.setAddress(client.getAddress());
                    if (client.getCountry() != null) existingClient.setCountry(client.getCountry());
                    if (client.getPhoneNumber() != null) existingClient.setPhoneNumber(client.getPhoneNumber());
                    if (client.getEmail() != null) existingClient.setEmail(client.getEmail());
                    if (client.getWebsite() != null) existingClient.setWebsite(client.getWebsite());
                    if (client.getLinkedInUrl() != null) existingClient.setLinkedInUrl(client.getLinkedInUrl());
                    if (client.getLogo() != null) existingClient.setLogo(client.getLogo());
                    if (client.getCompanyDescription() != null) existingClient.setCompanyDescription(client.getCompanyDescription());

                    existingClient.setUpdatedAt(LocalDateTime.now());
                    return clientRepository.save(existingClient);
                })
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + clientId));
    }

    @Override
    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Override
    public Client blockProfile(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(existingClient -> {
                    existingClient.setBlocked(true); // Set blocked to true
                    existingClient.setUpdatedAt(LocalDateTime.now()); // Update the timestamp
                    return clientRepository.save(existingClient);
                })
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + clientId));
    }

    // New methods implementation

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Optional<Client> getClientByUserId(String userId) {
        List<Client> clients = clientRepository.findByUserId(userId);
        return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
    }


    @Override
    @Transactional
    public Client updateClientProfileByUserId(String userId, UpdateClientDto client) {
        List<Client> clients = clientRepository.findByUserId(userId);
        if (clients.isEmpty()) {
            throw new ClientNotFoundException("Client not found with userId " + userId);
        }

        Client existingClient = clients.get(0);

        if (client.getCompanyName() != null) existingClient.setCompanyName(client.getCompanyName());
        if (client.getIndustry() != null) existingClient.setIndustry(client.getIndustry());
        if (client.getAddress() != null) existingClient.setAddress(client.getAddress());
        if (client.getCountry() != null) existingClient.setCountry(client.getCountry());
        if (client.getPhoneNumber() != null) existingClient.setPhoneNumber(client.getPhoneNumber());
        if (client.getEmail() != null) existingClient.setEmail(client.getEmail());
        if (client.getWebsite() != null) existingClient.setWebsite(client.getWebsite());
        if (client.getLinkedInUrl() != null) existingClient.setLinkedInUrl(client.getLinkedInUrl());
        if (client.getLogo() != null) existingClient.setLogo(client.getLogo());
        if (client.getCompanyDescription() != null) existingClient.setCompanyDescription(client.getCompanyDescription());

        // ⬇️ Mettre à jour le statut du profil à PENDING
        existingClient.setProfileStatus(ProfileStatus.PENDING);

        existingClient.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(existingClient);
    }


    @Override
    public Client blockProfileByUserId(String userId) {
        List<Client> clients = clientRepository.findByUserId(userId);
        if (clients.isEmpty()) {
            throw new ClientNotFoundException("Client not found with userId " + userId);
        }

        Client existingClient = clients.get(0);
        existingClient.setBlocked(true);
        existingClient.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(existingClient);
    }

    @Override
    @Transactional
    public void deleteClientById(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client not found with id " + clientId);
        }
        clientRepository.deleteById(clientId);
    }

    @Override
    @Transactional
    public void deleteClientByUserId(String userId) {
        List<Client> clients = clientRepository.findByUserId(userId);
        if (clients.isEmpty()) {
            throw new ClientNotFoundException("Client not found with userId " + userId);
        }
        clientRepository.delete(clients.get(0));
    }

    // LOGO UPLOAD METHODS
    @Override
    @Transactional
    public Client uploadLogo(UUID clientId, MultipartFile logoFile) throws Exception {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + clientId));

        // Validate file
        validateLogoFile(logoFile);

        // Generate unique filename
        String originalFilename = logoFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "logo_" + clientId + "_" + System.currentTimeMillis() + fileExtension;

        // Save file to disk
        Path filePath = Paths.get(uploadDirectory, uniqueFilename);
        Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update client with logo path
        String logoPath = "/uploads/logos/" + uniqueFilename;
        client.setLogo(logoPath);
        client.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client uploadLogoByUserId(String userId, MultipartFile logoFile) throws Exception {
        List<Client> clients = clientRepository.findByUserId(userId);
        if (clients.isEmpty()) {
            throw new ClientNotFoundException("Client not found with userId " + userId);
        }

        Client client = clients.get(0);

        // Validate file
        validateLogoFile(logoFile);

        // Generate unique filename
        String originalFilename = logoFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "logo_" + client.getClientId() + "_" + System.currentTimeMillis() + fileExtension;

        // Save file to disk
        Path filePath = Paths.get(uploadDirectory, uniqueFilename);
        Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update client with logo path
        String logoPath = "/uploads/logos/" + uniqueFilename;
        client.setLogo(logoPath);
        client.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(client);
    }

    private void validateLogoFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Check file size (limit to 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size should not exceed 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidImageExtension(filename)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG, and GIF files are allowed");
        }
    }

    private boolean isValidImageExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
    private String getEmailFromAuth(String userId) {
        try {
            return userClient.getUserEmail(userId).get("email");
        } catch (Exception e) {
            log.warn("⚠️ Failed to fetch email for userId {}", userId, e);
            return null;
        }
    }

    @Override
    public Optional<Client> getApprovedClientByCompanyName(String companyName) {
        return clientRepository.findByCompanyNameIgnoreCaseAndProfileStatus(
                companyName.trim(), ProfileStatus.APPROVED
        );
    }
}