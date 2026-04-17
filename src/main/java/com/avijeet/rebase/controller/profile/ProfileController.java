package com.avijeet.rebase.controller.profile;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.profile.UpdateProfileRequest;
import com.avijeet.rebase.dto.profile.UserProfileResponse;
import com.avijeet.rebase.service.profile.ProfileService;
import com.avijeet.rebase.utils.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Profiles", description = "User profile APIs")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get profile", description = "Fetches profile by user id")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ApiResponse.success("Profile fetched successfully", profileService.getProfile(userId));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile", description = "Updates current user's profile and optional avatar")
    @SecurityRequirement(name = "bearerAuth")
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