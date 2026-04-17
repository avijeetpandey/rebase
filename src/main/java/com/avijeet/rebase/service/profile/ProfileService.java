package com.avijeet.rebase.service.profile;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.profile.UpdateProfileRequest;
import com.avijeet.rebase.dto.profile.UserProfileResponse;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.entities.UserProfile;
import com.avijeet.rebase.exceptions.ResourceNotFoundException;
import com.avijeet.rebase.repository.profile.UserProfileRepository;
import com.avijeet.rebase.repository.user.UserRepository;
import com.avijeet.rebase.service.minio.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Transactional
    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found for userId: " + userId));
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    log.info("Auto-creating missing profile for userId: {}", userId);
                    return userProfileRepository.save(newProfile);
                });
        return mapToResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(AuthenticatedUser principal, UpdateProfileRequest request, MultipartFile avatar) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(principal.userId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (request.bio() != null) profile.setBio(request.bio());
        if (request.techStack() != null) profile.setTechStack(request.techStack());

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = minioService.uploadFile(avatar, "avatars");
            profile.setAvatarUrl(avatarUrl);
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Profile updated for userId: {}", principal.userId());
        return mapToResponse(savedProfile);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getTechStack()
        );
    }
}
