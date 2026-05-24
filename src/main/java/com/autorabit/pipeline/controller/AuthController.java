package com.autorabit.pipeline.controller;

import com.autorabit.pipeline.dto.ApiResponse;
import com.autorabit.pipeline.dto.LoginRequest;
import com.autorabit.pipeline.dto.LoginResponse;
import com.autorabit.pipeline.model.User;
import com.autorabit.pipeline.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication REST controller.
 *
 * POST /api/auth/login    — login with username/password, get JWT
 * GET  /api/auth/me       — get current user profile
 * POST /api/auth/logout   — client-side JWT invalidation (stateless)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        Map<String, Object> userInfo = Map.of(
                "id",          user.getId(),
                "username",    user.getUsername(),
                "email",       user.getEmail(),
                "fullName",    user.getFullName(),
                "avatarUrl",   user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                "roles",       user.getRoles(),
                "lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : "",
                "createdAt",   user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        );
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless — client should discard the JWT
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "valid", true,
                "username", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList()
        )));
    }
}
