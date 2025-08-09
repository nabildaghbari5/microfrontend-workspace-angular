package com.talentcloud.profile.service;

import com.talentcloud.profile.dto.UpdateCertificationDto;
import com.talentcloud.profile.iservice.IServiceCertification;
import com.talentcloud.profile.model.Candidate;
import com.talentcloud.profile.model.Certification;
import com.talentcloud.profile.repository.CertificationRepository;
import com.talentcloud.profile.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CertificationService implements IServiceCertification {

    private final CertificationRepository certificationRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository, CandidateRepository candidateRepository) {
        this.certificationRepository = certificationRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public Optional<Certification> getCertificationById(Long certificationId) {
        return certificationRepository.findById(certificationId);
    }

    @Override
    public Optional<Certification> getCertificationByIdForCurrentUser(Long certificationId, String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);
        Optional<Certification> certification = certificationRepository.findById(certificationId);
        if (certification.isPresent() && certification.get().getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            return certification;
        }
        return Optional.empty();
    }

    @Override
    public Page<Certification> getAllCertificationsByCandidateId(Long candidateId, Pageable pageable) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new IllegalArgumentException("Candidate not found with ID: " + candidateId);
        }
        return certificationRepository.findAllByCandidate_CandidateId(candidateId, pageable);
    }

    @Override
    public List<Certification> getAllCertificationsForCurrentUser(String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);
        return certificationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId());
    }

    @Override
    public Page<Certification> getAllCertificationsForCurrentUserPaginated(String userId, Pageable pageable) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);
        return certificationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId(), pageable);
    }

    @Override
    public Certification addCertification(Certification certification, Long candidateId) {
        return candidateRepository.findById(candidateId)
                .map(candidate -> {
                    certification.setCandidate(candidate);
                    certification.setCreatedAt(LocalDateTime.now());
                    certification.setUpdatedAt(LocalDateTime.now());
                    return certificationRepository.save(certification);
                })
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));
    }

    @Override
    public Certification addCertificationForCurrentUser(Certification certification, String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);
        certification.setCandidate(candidate);
        certification.setCreatedAt(LocalDateTime.now());
        certification.setUpdatedAt(LocalDateTime.now());
        return certificationRepository.save(certification);
    }

    @Override
    public Certification updateCertification(Long certificationId, UpdateCertificationDto dto) {
        Certification existingCertification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certificationId));

        existingCertification.setNom(dto.getNom());
        existingCertification.setOrganisme(dto.getOrganisme());
        existingCertification.setDateObtention(dto.getDateObtention());
        existingCertification.setDatevalidite(dto.getDatevalidite());
        existingCertification.setUrlVerification(dto.getUrlVerification());
        existingCertification.setUpdatedAt(LocalDateTime.now());

        return certificationRepository.save(existingCertification);
    }

    @Override
    public Certification updateCertificationForCurrentUser(Long certificationId, UpdateCertificationDto dto, String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);

        Certification existingCertification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certificationId));

        if (!existingCertification.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to update this certification");
        }

        existingCertification.setNom(dto.getNom());
        existingCertification.setOrganisme(dto.getOrganisme());
        existingCertification.setDateObtention(dto.getDateObtention());
        existingCertification.setDatevalidite(dto.getDatevalidite());
        existingCertification.setUrlVerification(dto.getUrlVerification());
        existingCertification.setUpdatedAt(LocalDateTime.now());

        return certificationRepository.save(existingCertification);
    }

    @Override
    public void deleteCertification(Long certificationId) {
        Certification existingCertification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certificationId));
        certificationRepository.delete(existingCertification);
    }

    @Override
    public void deleteCertificationForCurrentUser(Long certificationId, String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);

        Certification existingCertification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certificationId));

        if (!existingCertification.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new IllegalArgumentException("You don't have permission to delete this certification");
        }

        certificationRepository.delete(existingCertification);
    }
    @Override
    public Certification uploadCertificationFileForCurrentUser(MultipartFile file, String userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // Find candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No candidate profile found for current user");
        }
        Candidate candidate = candidates.get(0);

        // Prepare directory for uploads
        String baseDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "certifications";
        Path uploadDir = Paths.get(baseDir);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename, e.g., candidateId + timestamp
        String filename = "certification_" + candidate.getCandidateId() + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadDir.resolve(filename);

        Files.write(filePath, file.getBytes());

        // Create new Certification entity and set the file path
        Certification certification = new Certification();
        certification.setCandidate(candidate);
        certification.setCertificateFilePath(filePath.toString());
        certification.setCreatedAt(LocalDateTime.now());
        certification.setUpdatedAt(LocalDateTime.now());

        // Optional: you can set other default fields like nom or organisme if you want

        return certificationRepository.save(certification);
    }

    @Override
    public Certification createCertificationWithFile(
            String nom,
            String organisme,
            LocalDate dateObtention,
            LocalDate datevalidite,
            String urlVerification,
            MultipartFile file,
            String userId
    ) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier de certification est obligatoire");
        }

        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Aucun profil candidat trouvé pour l'utilisateur courant");
        }
        Candidate candidate = candidates.get(0);

        // Préparer le dossier upload
        String baseDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "certifications";
        Path uploadDir = Paths.get(baseDir);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "certification_" + candidate.getCandidateId() + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadDir.resolve(filename);

        Files.write(filePath, file.getBytes());

        // Créer et remplir la certification
        Certification certification = new Certification();
        certification.setNom(nom);
        certification.setOrganisme(organisme);
        certification.setDateObtention(dateObtention);
        certification.setDatevalidite(datevalidite);
        certification.setUrlVerification(urlVerification);
        certification.setCertificateFilePath(filePath.toString());
        certification.setCandidate(candidate);
        certification.setCreatedAt(LocalDateTime.now());
        certification.setUpdatedAt(LocalDateTime.now());

        return certificationRepository.save(certification);
    }

}
