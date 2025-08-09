//package com.talentcloud.profile.service;
//
//import com.talentcloud.profile.dto.UserProfileDto;
//import com.talentcloud.profile.model.UserProfile;
//import com.talentcloud.profile.repository.UserProfileRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ProfileService {
//
//    private final UserProfileRepository userProfileRepository;
//
//    @Cacheable(value = "profiles", key = "#id")
//    public UserProfileDto getProfile(String id) {
//        Optional<UserProfile> optional = userProfileRepository.findById(id);
//        return optional.map(this::toDto).orElse(null);
//    }
//
//    public void saveProfile(UserProfileDto dto) {
//        // todo: add ai extracted data & adapt request object
//        UserProfile profile = toEntity(dto);
//        if (profile.getId() == null || profile.getId().isBlank()) {
//            profile.setId(UUID.randomUUID().toString());
//        }
//        userProfileRepository.save(profile);
//    }
//
//    private UserProfileDto toDto(UserProfile profile) {
//        return new UserProfileDto(
//                profile.getId(),
//                profile.getName(),
//                profile.getEmail(),
//                profile.getJobTitle(),
//                profile.getLocation()
//        );
//    }
//
//    private UserProfile toEntity(UserProfileDto dto) {
//        return new UserProfile(
//                dto.getId(),
//                dto.getName(),
//                dto.getEmail(),
//                dto.getJobTitle(),
//                dto.getLocation()
//        );
//    }
//}
