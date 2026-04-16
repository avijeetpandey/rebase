package com.avijeet.rebase.controller;

import com.avijeet.rebase.dto.AuthResponse;
import com.avijeet.rebase.dto.LoginRequest;
import com.avijeet.rebase.dto.RegisterRequest;
import com.avijeet.rebase.dto.UserProfileResponse;
import com.avijeet.rebase.service.auth.AuthService;
import com.avijeet.rebase.service.auth.JwtService;
import com.avijeet.rebase.utils.api.ApiResponse;
import com.avijeet.rebase.utils.constants.ApiConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.AUTH_BASE_URL)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse registeredUser = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthResponse authResponse = authService.login(
                request,
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            Authentication authentication,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        UUID sessionId = UUID.fromString(jwtService.extractSessionId(token));
        UserProfileResponse user = authService.getCurrentUser(authentication.getName(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Current user fetched", user));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        UUID sessionId = UUID.fromString(jwtService.extractSessionId(token));
        authService.logout(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}

