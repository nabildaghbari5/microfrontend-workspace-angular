package com.talentcloud.profile.service;

import com.talentcloud.profile.client.UserClient;
import com.talentcloud.profile.dto.*;
import com.talentcloud.profile.dto.event.ProfileCreatedEvent;
import com.talentcloud.profile.dto.event.ProfileStatusChangedEvent;
import com.talentcloud.profile.exception.CandidateNotFoundException;
import com.talentcloud.profile.iservice.IServiceCandidate;
import com.talentcloud.profile.kafka.ProfileEventProducer;
import com.talentcloud.profile.model.*;
import com.talentcloud.profile.repository.*;
import com.talentcloud.profile.strategy.BlockProfileStrategy;
import com.talentcloud.profile.strategy.PrivateProfileStrategy;
import com.talentcloud.profile.strategy.ProfileStrategy;
import com.talentcloud.profile.strategy.VisibleProfileStrategy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

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
import java.util.stream.Collectors;

@Slf4j
@Service
public class CandidateService implements IServiceCandidate {

    private final CandidateRepository candidateRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificationRepository certificationRepository;
    private final SkillsRepository skillRepository;
    private final EducationService educationService;
    private final ExperienceService experienceService;
    private final SkillsService skillsService;
    private final CertificationService certificationService;
    private final UserClient userClient;
    private final ProfileEventProducer eventProducer;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository,
                            EducationRepository educationRepository,
                            ExperienceRepository experienceRepository,
                            CertificationRepository certificationRepository,
                            SkillsRepository skillRepository,
                            EducationService educationService,
                            ExperienceService experienceService,
                            SkillsService skillsService,
                            CertificationService certificationService,
                            UserClient userClient,
                            ProfileEventProducer eventProducer) {
        this.candidateRepository = candidateRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.certificationRepository = certificationRepository;
        this.skillRepository = skillRepository;
        this.educationService = educationService;
        this.experienceService = experienceService;
        this.skillsService = skillsService;
        this.certificationService = certificationService;
        this.userClient = userClient;
        this.eventProducer = eventProducer;
    }

    @Override
    public Optional<Candidate> getCandidateByUserId(String userId) {
        return candidateRepository.findByUserId(userId).stream().findFirst();
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
    public CandidateResponse findCandidateByUserId(String userId) {
        List<Candidate> candidates = candidateRepository.findByUserId(userId);

        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("Candidate not found with userId: " + userId);
        }

        // Get the first candidate (there should only be one per userId based on your createCandidateProfile logic)
        Candidate candidate = candidates.get(0);

        // Load all related data for the candidate
        candidate.setEducations(educationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
        candidate.setExperiences(experienceRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
        candidate.setCertifications(certificationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
        candidate.setSkills(skillRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));

        return mapToCandidateResponse(candidate);
    }

    @Override
    public Candidate createCandidateProfile(Candidate candidate, String userId) {
        // Check if a profile already exists for this userId
        List<Candidate> existingCandidates = candidateRepository.findByUserId(userId);
        if (!existingCandidates.isEmpty()) {
            throw new IllegalStateException("User already has a candidate profile");
        }

        // Set the userId from header to the candidate entity
        candidate.setUserId(userId);

        // Set default visibility
        if (candidate.getVisibilitySettings() == null) {
            candidate.setVisibilitySettings(VisibilitySettings.PUBLIC);
        }

        // Set default status to PENDING
        candidate.setProfileStatus(ProfileStatus.PENDING);

        // Set timestamps
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        // Save the candidate with the userId from the header
        return candidateRepository.save(candidate);
    }

    @Override
    @Transactional
    public Candidate createCompleteProfile(CreateProfileDto request, String userId) throws Exception {
        // 1. Create candidate from DTO
        Candidate candidate = new Candidate();
        candidate.setAboutMe(request.getAboutMe());
        candidate.setJobTitle(request.getJobTitle());
        candidate.setJobPreferences(request.getJobPreferences());
        candidate.setProfilePicture(request.getProfilePicture());
        candidate.setResume(request.getResume());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setPhoneNumber(request.getPhoneNumber());
        candidate.setGender(request.getGender());
        candidate.setLinkedInUrl(request.getLinkedInUrl());
        candidate.setPortfolioUrl(request.getPortfolioUrl());
        candidate.setAddress(request.getAddress());
        candidate.setLocation(request.getLocation());
        candidate.setVisibilitySettings(request.getVisibilitySettings() != null ?
                request.getVisibilitySettings() : VisibilitySettings.PUBLIC);

        // 2. Save the candidate profile (status will be automatically set to PENDING)
        Candidate savedCandidate = createCandidateProfile(candidate, userId);
        Long candidateId = savedCandidate.getCandidateId();

        // 3. Create education entries if present
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            for (EducationDto eduDto : request.getEducations()) {
                Education education = new Education();
                education.setInstitution(eduDto.getInstitution());
                education.setDiplome(eduDto.getDiplome());
                education.setDomaineEtude(eduDto.getDomaineEtude());
                education.setDateDebut(eduDto.getDateDebut());
                education.setDateFin(eduDto.getDateFin());
                education.setMoyenne(eduDto.getMoyenne());
                education.setEnCours(eduDto.getEnCours());

                educationService.addEducation(education, candidateId);
            }
        }

        // 4. Create experience entries if present
        if (request.getExperiences() != null && !request.getExperiences().isEmpty()) {
            for (ExperienceDto expDto : request.getExperiences()) {
                Experience experience = new Experience();
                experience.setTitrePoste(expDto.getTitrePoste());
                experience.setEntreprise(expDto.getEntreprise());
                experience.setDateDebut(expDto.getDateDebut());
                experience.setDateFin(expDto.getDateFin());
                experience.setDescription(expDto.getDescription());
                experience.setLieu(expDto.getLieu());
                experience.setTypeContrat(expDto.getTypeContrat());
                experience.setTechnologies(expDto.getTechnologies());
                experience.setEnCours(expDto.getEnCours());
                experience.setSiteEntreprise(expDto.getSiteEntreprise());

                experienceService.createExperience(experience, candidateId);
            }
        }

        // 5. Create skills if present
        if (request.getSkills() != null) {
            SkillsDto skillsDto = request.getSkills();
            Skills skills = new Skills();
            skills.setProgrammingLanguages(skillsDto.getProgrammingLanguages());
            skills.setSoftSkills(skillsDto.getSoftSkills());
            skills.setTechnicalSkills(skillsDto.getTechnicalSkills());
            skills.setToolsAndTechnologies(skillsDto.getToolsAndTechnologies());
            skills.setCustomSkills(skillsDto.getCustomSkills());

            skillsService.addSkills(skills, candidateId);
        }

        // 6. Create certifications if present (important: save them)

        // 7. Send ProfileCreatedEvent to notification-ms
        try {
            String userEmail;

            // Try to get email from auth-ms with robust error handling
            try {
                log.info("🔍 Attempting to fetch email for userId: {}", userId);
                Map<String, String> userInfo = userClient.getUserEmail(userId);
                log.info("🔍 Response from auth-ms: {}", userInfo); // 👈 Ajoute ceci
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

            // Create and send the event
            ProfileCreatedEvent event = ProfileCreatedEvent.builder()
                    .userId(userId)
                    .email(userEmail)
                    .firstname(null) // or get from userInfo if available
                    .profileType("CANDIDATE")
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            eventProducer.sendProfileCreatedEvent(event);
            log.info("✅ ProfileCreatedEvent sent for candidate: {} with email: {}", userId, userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send ProfileCreatedEvent for candidate: {}", userId, e);
            // Don't throw exception here - profile creation should succeed even if event fails
            log.info("🔄 Profile creation continues despite event failure for userId: {}", userId);
        }

        // 8. Fetch and return the complete profile
        Optional<Candidate> completeProfile = getCandidateById(candidateId);
        return completeProfile.orElse(savedCandidate);
    }

    // Add this helper method to your CandidateService class
    private String generateFallbackEmail(String userId) {
        // Generate a meaningful fallback email that your notification service can handle
        String shortUserId = userId.length() > 8 ? userId.substring(0, 8) : userId;
        return "pending-user-" + shortUserId + "@talentcloud.com";
    }

    // NEW METHOD: Upload profile picture
    @Override
    @Transactional
    public String uploadProfilePicture(String userId, MultipartFile file) throws Exception {
        // Find candidate by userId
        List<Candidate> candidates = candidateRepository.findByUserId(userId);
        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("No profile found for this user");
        }

        Candidate candidate = candidates.get(0);

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type (only allow image files)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        try {
            // Create upload directory using System.getProperty("user.dir")
            String projectRoot = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectRoot, "uploads", "profile-pictures");

            // Create directories if they don't exist
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadDir.resolve(uniqueFilename);

            // Delete old profile picture if exists
            if (candidate.getProfilePicture() != null && !candidate.getProfilePicture().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get(projectRoot, candidate.getProfilePicture());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    // Log warning but don't fail the upload
                    System.err.println("Warning: Could not delete old profile picture: " + e.getMessage());
                }
            }

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update candidate with relative path
            String relativePath = "uploads/profile-pictures/" + uniqueFilename;
            candidate.setProfilePicture(relativePath);
            candidate.setUpdatedAt(LocalDateTime.now());
            candidateRepository.save(candidate);

            return relativePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
        }
    }

    // NEW METHODS FOR STATUS MANAGEMENT
    @Override
    @Transactional
    public Candidate approveCandidate(Long candidateId) throws Exception {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        candidate.setProfileStatus(ProfileStatus.APPROVED);
        candidate.setRejectionReason(null);
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate saved = candidateRepository.save(candidate);

        // 🔥 Send ProfileStatusChangedEvent (APPROVED)
        String email = getEmailFromAuth(candidate.getUserId());
        eventProducer.sendProfileStatusChangedEvent(
                ProfileStatusChangedEvent.builder()
                        .userId(candidate.getUserId())
                        .userEmail(email)
                        .userType("CANDIDATE")
                        .profileStatus(ProfileStatus.APPROVED.name())
                        .message("🎉 Your profile is approved. You can now apply for jobs.")
                        .build()
        );

        return saved;
    }

    @Override
    @Transactional
    public Candidate rejectCandidate(Long candidateId, String rejectionReason) throws Exception {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        candidate.setProfileStatus(ProfileStatus.REJECTED);
        candidate.setRejectionReason(rejectionReason);
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate saved = candidateRepository.save(candidate);

        // 🔥 Send ProfileStatusChangedEvent (REJECTED)
        String email = getEmailFromAuth(candidate.getUserId());
        eventProducer.sendProfileStatusChangedEvent(
                ProfileStatusChangedEvent.builder()
                        .userId(candidate.getUserId())
                        .userEmail(email)
                        .userType("CANDIDATE")
                        .profileStatus(ProfileStatus.REJECTED.name())
                        .message("❌ Your profile has been rejected. Reason: " + rejectionReason)
                        .build()
        );

        return saved;
    }

    @Override
    @Transactional
    public Candidate blockProfile(Long candidateId) throws Exception {
        Candidate existingCandidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        // Apply the BlockProfileStrategy
        existingCandidate.setProfileStrategy(new BlockProfileStrategy());
        existingCandidate.applyProfileStrategy(); // Apply the strategy
        existingCandidate.setBlocked(true); // Set the blocked flag

        existingCandidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(existingCandidate);
    }

    @Override
    @Transactional
    public Optional<Candidate> getCandidateById(Long candidateId) {
        Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);

        if (candidateOptional.isPresent()) {
            Candidate candidate = candidateOptional.get();

            // Fetch and set all related information
            candidate.setEducations(educationRepository.findAllByCandidate_CandidateId(candidateId));
            candidate.setExperiences(experienceRepository.findAllByCandidate_CandidateId(candidateId));
            candidate.setCertifications(certificationRepository.findAllByCandidate_CandidateId(candidateId));
            candidate.setSkills(skillRepository.findAllByCandidate_CandidateId(candidateId));

            return Optional.of(candidate);
        }

        return candidateOptional;
    }

    @Override
    @Transactional
    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();

        // Load related data for each candidate
        for (Candidate candidate : candidates) {
            candidate.setEducations(educationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
            candidate.setExperiences(experienceRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
            candidate.setCertifications(certificationRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
            candidate.setSkills(skillRepository.findAllByCandidate_CandidateId(candidate.getCandidateId()));
        }

        return candidates;
    }

    @Override
    @Transactional
    public Candidate editCandidateProfile(Long candidateId, UpdateCandidateDto dto) throws Exception {
        Candidate existingCandidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        // Update basic fields
        if (dto.getProfilePicture() != null) existingCandidate.setProfilePicture(dto.getProfilePicture());
        if (dto.getResume() != null) existingCandidate.setResume(dto.getResume());
        if (dto.getJobPreferences() != null) existingCandidate.setJobPreferences(dto.getJobPreferences());
        if (dto.getJobTitle() != null) existingCandidate.setJobTitle(dto.getJobTitle());

        // Update new fields
        if (dto.getAboutMe() != null) existingCandidate.setAboutMe(dto.getAboutMe());
        if (dto.getDateOfBirth() != null) existingCandidate.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getPhoneNumber() != null) existingCandidate.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getGender() != null) existingCandidate.setGender(dto.getGender());
        if (dto.getLinkedInUrl() != null) existingCandidate.setLinkedInUrl(dto.getLinkedInUrl());
        if (dto.getPortfolioUrl() != null) existingCandidate.setPortfolioUrl(dto.getPortfolioUrl());
        if (dto.getAddress() != null) existingCandidate.setAddress(dto.getAddress());
        if (dto.getLocation() != null) existingCandidate.setLocation(dto.getLocation());

        // Mettre le profil en attente de validation
        existingCandidate.setProfileStatus(ProfileStatus.PENDING);

        existingCandidate.setUpdatedAt(LocalDateTime.now());

        Candidate savedCandidate = candidateRepository.save(existingCandidate);

        // Update educations if present
        if (dto.getEducations() != null && !dto.getEducations().isEmpty()) {
            educationRepository.deleteAllByCandidate_CandidateId(candidateId);
            for (EducationDto eduDto : dto.getEducations()) {
                Education education = new Education();
                education.setInstitution(eduDto.getInstitution());
                education.setDiplome(eduDto.getDiplome());
                education.setDomaineEtude(eduDto.getDomaineEtude());
                education.setDateDebut(eduDto.getDateDebut());
                education.setDateFin(eduDto.getDateFin());
                education.setMoyenne(eduDto.getMoyenne());
                education.setEnCours(eduDto.getEnCours());

                educationService.addEducation(education, candidateId);
            }
        }

        // Update experiences if present
        if (dto.getExperiences() != null && !dto.getExperiences().isEmpty()) {
            experienceRepository.deleteAllByCandidate_CandidateId(candidateId);
            for (ExperienceDto expDto : dto.getExperiences()) {
                Experience experience = new Experience();
                experience.setTitrePoste(expDto.getTitrePoste());
                experience.setEntreprise(expDto.getEntreprise());
                experience.setDateDebut(expDto.getDateDebut());
                experience.setDateFin(expDto.getDateFin());
                experience.setDescription(expDto.getDescription());
                experience.setLieu(expDto.getLieu());
                experience.setTypeContrat(expDto.getTypeContrat());
                experience.setTechnologies(expDto.getTechnologies());
                experience.setEnCours(expDto.getEnCours());
                experience.setSiteEntreprise(expDto.getSiteEntreprise());

                experienceService.createExperience(experience, candidateId);
            }
        }

        // Update skills if present
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            skillRepository.deleteAllByCandidate_CandidateId(candidateId);

            List<SkillsDto> skillsList = dto.getSkills();

            for (SkillsDto skillsDto : skillsList) {
                Skills skills = new Skills();
                skills.setProgrammingLanguages(skillsDto.getProgrammingLanguages());
                skills.setSoftSkills(skillsDto.getSoftSkills());
                skills.setTechnicalSkills(skillsDto.getTechnicalSkills());
                skills.setToolsAndTechnologies(skillsDto.getToolsAndTechnologies());
                skills.setCustomSkills(skillsDto.getCustomSkills());

                skillsService.addSkills(skills, candidateId);
            }
        }
        // Update certifications if present
        if (dto.getCertifications() != null && !dto.getCertifications().isEmpty()) {
            certificationRepository.deleteAllByCandidate_CandidateId(candidateId);
            for (UpdateCertificationDto certDto : dto.getCertifications()) {
                Certification certification = new Certification();
                certification.setNom(certDto.getNom());
                certification.setOrganisme(certDto.getOrganisme());
                certification.setDateObtention(certDto.getDateObtention());
                certification.setDatevalidite(certDto.getDatevalidite());
                certification.setUrlVerification(certDto.getUrlVerification());

                certificationService.addCertification(certification, candidateId);
            }
        }

        // Fetch and attach related info
        savedCandidate.setEducations(educationRepository.findAllByCandidate_CandidateId(candidateId));
        savedCandidate.setExperiences(experienceRepository.findAllByCandidate_CandidateId(candidateId));
        savedCandidate.setCertifications(certificationRepository.findAllByCandidate_CandidateId(candidateId));
        savedCandidate.setSkills(skillRepository.findAllByCandidate_CandidateId(candidateId));

        return savedCandidate;
    }


    // Helper method to select the correct strategy based on visibility setting
    private ProfileStrategy getProfileStrategy(VisibilitySettings visibility) {
        switch (visibility) {
            case PUBLIC:
                return new VisibleProfileStrategy();
            case PRIVATE:
                return new PrivateProfileStrategy();
            case RESTRICTED:
                return new BlockProfileStrategy();
            default:
                throw new IllegalArgumentException("Unknown visibility setting: " + visibility);
        }
    }

    @Override
    public CandidateResponse findCandidateById(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .map(this::mapToCandidateResponse)  // Using the mapToCandidateResponse method
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with ID : " + candidateId));
    }

    @Override
    @Transactional
    public Candidate updateVisibility(Long candidateId, VisibilitySettings visibility) throws Exception {
        Candidate existingCandidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        // Apply the correct strategy based on visibility setting
        existingCandidate.setProfileStrategy(getProfileStrategy(visibility));
        existingCandidate.applyProfileStrategy();  // Apply the selected strategy
        existingCandidate.setVisibilitySettings(visibility); // Set the visibility setting

        existingCandidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(existingCandidate);
    }

    @Override
    public List<Candidate> findByUserId(String userId) {
        return candidateRepository.findByUserId(userId);
    }

    /**
     * Deletes a candidate profile and all associated data
     * @param candidateId the ID of the candidate to delete
     * @throws Exception if the candidate doesn't exist or deletion fails
     */
    @Override
    @Transactional
    public void deleteCandidate(Long candidateId) throws Exception {
        // Check if candidate exists
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id " + candidateId));

        // Delete profile picture file if exists
        if (candidate.getProfilePicture() != null && !candidate.getProfilePicture().isEmpty()) {
            try {
                String projectRoot = System.getProperty("user.dir");
                Path filePath = Paths.get(projectRoot, candidate.getProfilePicture());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log warning but don't fail the deletion
                System.err.println("Warning: Could not delete profile picture file: " + e.getMessage());
            }
        }

        // Delete all related data first (cascade would handle this, but doing it explicitly for clarity)
        educationRepository.deleteAllByCandidate_CandidateId(candidateId);
        experienceRepository.deleteAllByCandidate_CandidateId(candidateId);
        certificationRepository.deleteAllByCandidate_CandidateId(candidateId);
        skillRepository.deleteAllByCandidate_CandidateId(candidateId);

        // Finally delete the candidate
        candidateRepository.delete(candidate);
    }

    // Update your existing mapToCandidateResponse method to include userId mapping
    // Replace your existing mapToCandidateResponse method with this updated version
    public CandidateResponse mapToCandidateResponse(Candidate candidate) {
        CandidateResponse candidateResponse = new CandidateResponse();

        // Mapping basic candidate details
        candidateResponse.setCandidateId(candidate.getCandidateId());
        candidateResponse.setUserId(candidate.getUserId()); // Add this line
        candidateResponse.setProfilePicture(candidate.getProfilePicture());
        candidateResponse.setResume(candidate.getResume());
        candidateResponse.setJobPreferences(candidate.getJobPreferences());
        candidateResponse.setJobTitle(candidate.getJobTitle());
        candidateResponse.setAboutMe(candidate.getAboutMe());
        candidateResponse.setDateOfBirth(candidate.getDateOfBirth());
        candidateResponse.setPhoneNumber(candidate.getPhoneNumber());

        // Handle gender safely (check for null)
        if (candidate.getGender() != null) {
            candidateResponse.setGender(candidate.getGender().toString());
        }

        candidateResponse.setLinkedInUrl(candidate.getLinkedInUrl());
        candidateResponse.setPortfolioUrl(candidate.getPortfolioUrl());
        candidateResponse.setAddress(candidate.getAddress());
        candidateResponse.setLocation(candidate.getLocation());

        // Mapping certifications (handle null collections)
        if (candidate.getCertifications() != null) {
            candidateResponse.setCertifications(
                    candidate.getCertifications().stream()
                            .map(cert -> {
                                CertificationResponse certResponse = new CertificationResponse();
                                certResponse.setId(cert.getId());
                                certResponse.setNom(cert.getNom());
                                certResponse.setOrganisme(cert.getOrganisme());
                                certResponse.setDateObtention(cert.getDateObtention());
                                certResponse.setDatevalidite(cert.getDatevalidite());
                                certResponse.setUrlVerification(cert.getUrlVerification());
                                return certResponse;
                            })
                            .collect(Collectors.toList())
            );
        }

        // Mapping educations (handle null collections)
        if (candidate.getEducations() != null) {
            candidateResponse.setEducations(
                    candidate.getEducations().stream()
                            .map(edu -> {
                                EducationResponse eduResponse = new EducationResponse();
                                eduResponse.setId(edu.getId());
                                eduResponse.setDiplome(edu.getDiplome());
                                eduResponse.setInstitution(edu.getInstitution());
                                eduResponse.setDateDebut(edu.getDateDebut());
                                eduResponse.setDateFin(edu.getDateFin());
                                eduResponse.setEnCours(edu.getEnCours());
                                eduResponse.setMoyenne(edu.getMoyenne());
                                eduResponse.setDomaineEtude(edu.getDomaineEtude());
                                return eduResponse;
                            })
                            .collect(Collectors.toList())
            );
        }

        // Mapping skills (handle null collections)
        if (candidate.getSkills() != null) {
            candidateResponse.setSkills(
                    candidate.getSkills().stream()
                            .map(skill -> {
                                SkillsResponse skillsResponse = new SkillsResponse();
                                skillsResponse.setId(skill.getId());
                                skillsResponse.setProgrammingLanguages(skill.getProgrammingLanguages());
                                skillsResponse.setSoftSkills(skill.getSoftSkills());
                                skillsResponse.setTechnicalSkills(skill.getTechnicalSkills());
                                skillsResponse.setToolsAndTechnologies(skill.getToolsAndTechnologies());
                                skillsResponse.setCustomSkills(skill.getCustomSkills());
                                return skillsResponse;
                            })
                            .collect(Collectors.toList())
            );
        }

        // Mapping experience (handle null collections)
        if (candidate.getExperiences() != null) {
            candidateResponse.setExperiences(
                    candidate.getExperiences().stream()
                            .map(exp -> {
                                ExperienceResponse experienceResponse = new ExperienceResponse();
                                experienceResponse.setId(exp.getId());
                                experienceResponse.setTitrePoste(exp.getTitrePoste());
                                experienceResponse.setEntreprise(exp.getEntreprise());
                                experienceResponse.setDateDebut(exp.getDateDebut());
                                experienceResponse.setDateFin(exp.getDateFin());
                                experienceResponse.setDescription(exp.getDescription());
                                experienceResponse.setLieu(exp.getLieu());
                                experienceResponse.setTypeContrat(exp.getTypeContrat());
                                experienceResponse.setTechnologies(exp.getTechnologies());
                                experienceResponse.setEnCours(exp.getEnCours());
                                experienceResponse.setSiteEntreprise(exp.getSiteEntreprise());
                                return experienceResponse;
                            })
                            .collect(Collectors.toList())
            );
        }

        return candidateResponse;
    }
}