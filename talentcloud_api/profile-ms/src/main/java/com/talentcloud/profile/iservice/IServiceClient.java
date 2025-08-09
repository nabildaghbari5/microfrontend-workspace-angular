package com.talentcloud.profile.iservice;

import com.talentcloud.profile.dto.UpdateClientDto;
import com.talentcloud.profile.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServiceClient {
    Client createClientProfile(Client client);
    Client createClientProfile(Client client, String userId);
    Optional<Client> getClientById(UUID clientId);
    Client updateClientProfile(UUID clientId, UpdateClientDto client);
    Page<Client> getAllClients(Pageable pageable);
    Client blockProfile(UUID clientId);
    String getClientEmailByUserId(String userId);

    // New methods
    List<Client> getAllClients();
    Optional<Client> getClientByUserId(String userId);
    Client updateClientProfileByUserId(String userId, UpdateClientDto client);
    Client blockProfileByUserId(String userId);
    void deleteClientById(UUID clientId);
    void deleteClientByUserId(String userId);

    // NEW METHODS FOR STATUS MANAGEMENT
    Client approveClient(UUID clientId) throws Exception;
    Client rejectClient(UUID clientId, String rejectionReason) throws Exception;

    // LOGO UPLOAD METHODS
    Client uploadLogo(UUID clientId, MultipartFile logoFile) throws Exception;
    Client uploadLogoByUserId(String userId, MultipartFile logoFile) throws Exception;

    Optional<Client> getApprovedClientByCompanyName(String companyName);

}