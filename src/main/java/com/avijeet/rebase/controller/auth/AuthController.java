package com.avijeet.rebase.controller.auth;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.auth.AuthResponse;
import com.avijeet.rebase.dto.auth.LoginRequest;
import com.avijeet.rebase.dto.auth.RefreshTokenRequest;
import com.avijeet.rebase.dto.auth.RegisterRequest;
import com.avijeet.rebase.dto.profile.UserProfileResponse;
import com.avijeet.rebase.service.auth.AuthService;
import com.avijeet.rebase.utils.api.ApiResponse;
import com.avijeet.rebase.utils.constants.ApiConstants;
import com.avijeet.rebase.utils.http.RequestContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.AUTH_BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Authentication and session lifecycle APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new user and corresponding profile")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for username={}", request.username());
        UserProfileResponse registeredUser = authService.register(request);
        log.info("User registered successfully userId={} username={}", registeredUser.id(), registeredUser.username());
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", registeredUser));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and creates a persisted session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = RequestContextUtils.resolveClientIp(httpServletRequest);
        log.info("Login request received for username={} from ip={}", request.username(), clientIp);
        AuthResponse authResponse = authService.login(
                request,
                httpServletRequest.getHeader("User-Agent"),
                clientIp
        );
        log.info("Login successful for username={} sessionId={}", authResponse.user().username(), authResponse.sessionId());
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Current user", description = "Returns currently authenticated user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        log.debug("Fetching profile for username={} sessionId={}", principal.username(), principal.sessionId());
        UserProfileResponse user = authService.getCurrentUser(principal.username(), principal.sessionId());
        return ResponseEntity.ok(ApiResponse.success("Current user fetched", user));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Creates a new access token from refresh token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access token refreshed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        AuthResponse authResponse = authService.refreshAccessToken(request);
        log.info("Access token refreshed for username={} sessionId={}", authResponse.user().username(), authResponse.sessionId());
        return ResponseEntity.ok(ApiResponse.success("Access token refreshed", authResponse));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Invalidates active persisted session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Logout request for username={} sessionId={}", principal.username(), principal.sessionId());
        authService.logout(principal.sessionId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}

