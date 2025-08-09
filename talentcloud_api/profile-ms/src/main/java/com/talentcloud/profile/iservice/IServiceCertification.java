package com.talentcloud.profile.iservice;

import com.talentcloud.profile.dto.UpdateCertificationDto;
import com.talentcloud.profile.model.Certification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IServiceCertification {
    // Original methods
    Optional<Certification> getCertificationById(Long certificationId);
    Page<Certification> getAllCertificationsByCandidateId(Long candidateId, Pageable pageable);
    Certification addCertification(Certification certification, Long candidateId);
    Certification updateCertification(Long certificationId, UpdateCertificationDto dto);
    void deleteCertification(Long certificationId);

    // New methods using userId from header
    Optional<Certification> getCertificationByIdForCurrentUser(Long certificationId, String userId);
    List<Certification> getAllCertificationsForCurrentUser(String userId);
    Page<Certification> getAllCertificationsForCurrentUserPaginated(String userId, Pageable pageable);
    Certification addCertificationForCurrentUser(Certification certification, String userId);
    Certification updateCertificationForCurrentUser(Long certificationId, UpdateCertificationDto dto, String userId);
    void deleteCertificationForCurrentUser(Long certificationId, String userId);
    Certification uploadCertificationFileForCurrentUser(MultipartFile file, String userId) throws Exception;
    Certification createCertificationWithFile(
            String nom,
            String organisme,
            LocalDate dateObtention,
            LocalDate datevalidite,
            String urlVerification,
            MultipartFile file,
            String userId
    ) throws Exception;
}
