package com.management.restaurant.controller;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.dto.oauth2.LinkOAuth2Request;
import com.management.restaurant.dto.oauth2.OAuth2LinkDTO;
import com.management.restaurant.dto.oauth2.OAuth2LinkResponse;
import com.management.restaurant.service.OAuth2Service;
import com.management.restaurant.service.oauth2.OAuth2LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 Authentication and Management Controller
 */
@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2 Authentication", description = "OAuth2 login and account linking")
public class OAuth2Controller {
    private final OAuth2Service oauth2Service;
    private final OAuth2LinkService oauth2LinkService;

    // ===== AUTHENTICATION =====

    @PostMapping("/login")
    @Operation(summary = "OAuth2 login",
            description = "Authenticate user with OAuth2 provider (Google, Facebook, etc.)")
    public ResponseEntity<AuthResponse> oauth2Login(
            @Valid @RequestBody OAuth2LoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/oauth2/login - Provider: {}", request.getProvider());

        AuthResponse response = oauth2Service.authenticateOAuth2User(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh OAuth2 token")
    public ResponseEntity<AuthResponse> refreshOAuth2Token(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        log.info("POST /api/oauth2/refresh");

        String refreshToken = authHeader.replace("Bearer ", "");
        AuthResponse response = oauth2Service.refreshOAuth2Token(refreshToken, httpRequest);

        return ResponseEntity.ok(response);
    }

    // ===== ACCOUNT LINKING =====

    @PostMapping("/link")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Link OAuth2 account",
            description = "Link an OAuth2 provider to existing account")
    public ResponseEntity<OAuth2LinkResponse> linkOAuth2Account(
            @Valid @RequestBody LinkOAuth2Request request,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("POST /api/oauth2/link - User: {}, Provider: {}",
                username, request.getProvider());

        // Get user ID from authentication
        Long userId = getUserIdFromAuth(authentication);

        OAuth2LoginRequest oauth2Request = new OAuth2LoginRequest();
        oauth2Request.setProvider(request.getProvider());
        oauth2Request.setAccessToken(request.getAccessToken());
        oauth2Request.setIdToken(request.getIdToken());

        oauth2Service.linkOAuth2Account(userId, oauth2Request);

        return ResponseEntity.ok(OAuth2LinkResponse.builder()
                .success(true)
                .message("OAuth2 account linked successfully")
                .build());
    }

    @DeleteMapping("/unlink/{provider}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unlink OAuth2 account")
    public ResponseEntity<OAuth2LinkResponse> unlinkOAuth2Account(
            @PathVariable String provider,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("DELETE /api/oauth2/unlink/{} - User: {}", provider, username);

        Long userId = getUserIdFromAuth(authentication);
        oauth2LinkService.unlinkAccount(userId, provider);

        return ResponseEntity.ok(OAuth2LinkResponse.builder()
                .success(true)
                .message("OAuth2 account unlinked successfully")
                .build());
    }

    // ===== ACCOUNT MANAGEMENT =====

    @GetMapping("/linked")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get linked OAuth2 accounts")
    public ResponseEntity<List<OAuth2LinkDTO>> getLinkedAccounts(
            Authentication authentication) {

        String username = authentication.getName();
        log.info("GET /api/oauth2/linked - User: {}", username);

        Long userId = getUserIdFromAuth(authentication);
        List<OAuth2LinkDTO> links = oauth2LinkService.getLinkedProviders(userId);

        return ResponseEntity.ok(links);
    }

    @PutMapping("/set-primary/{provider}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set primary OAuth2 provider")
    public ResponseEntity<OAuth2LinkResponse> setPrimaryProvider(
            @PathVariable String provider,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("PUT /api/oauth2/set-primary/{} - User: {}", provider, username);

        Long userId = getUserIdFromAuth(authentication);
        oauth2LinkService.setPrimaryProvider(userId, provider);

        return ResponseEntity.ok(OAuth2LinkResponse.builder()
                .success(true)
                .message("Primary provider updated")
                .build());
    }

    @GetMapping("/check/{provider}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Check if provider is linked")
    public ResponseEntity<Map<String, Serializable>> checkLinkedProvider(
            @PathVariable String provider,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        boolean isLinked = oauth2LinkService.hasLinkedProvider(userId, provider);

        return ResponseEntity.ok(Map.of(
                "provider", provider,
                "isLinked", isLinked
        ));
    }

    // ===== ADMIN OPERATIONS =====

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get OAuth2 statistics (admin only)")
    public ResponseEntity<Map<String, Long>> getOAuth2Statistics() {
        log.info("GET /api/oauth2/admin/statistics");

        Map<String, Long> stats = oauth2LinkService.getProviderStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/admin/cleanup-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cleanup expired OAuth2 tokens (admin only)")
    public ResponseEntity<Map<String, Object>> cleanupExpiredTokens() {
        log.info("POST /api/oauth2/admin/cleanup-tokens");

        int cleaned = oauth2LinkService.cleanupExpiredTokens();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Expired tokens cleaned up",
                "count", cleaned,
                "timestamp", LocalDateTime.now()
        ));
    }

    // ===== HELPER METHODS =====

    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Implement based on your UserDetails implementation
        // This is a placeholder
        return 1L;
    }
}