package com.talentcloud.profile.repository;

import com.talentcloud.profile.model.Client;
import com.talentcloud.profile.model.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByUserId(String userId);
    Optional<Client> findByCompanyNameIgnoreCaseAndProfileStatus(String companyName, ProfileStatus profileStatus);

}