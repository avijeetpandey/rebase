package com.avijeet.rebase.controller.profile;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.profile.UpdateProfileRequest;
import com.avijeet.rebase.dto.profile.UserProfileResponse;
import com.avijeet.rebase.service.profile.ProfileService;
import com.avijeet.rebase.utils.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(com.avijeet.rebase.utils.constants.ApiConstants.API_BASE_URL + "/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ApiResponse.success("Profile fetched successfully", profileService.getProfile(userId));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateProfile(
            Authentication authentication,
            @RequestPart(value = "request", required = false) @Valid UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assert principal != null;
        return ApiResponse.success("Profile updated successfully",
                profileService.updateProfile(principal, request != null ? request : new UpdateProfileRequest(null, null), avatar));
    }
}