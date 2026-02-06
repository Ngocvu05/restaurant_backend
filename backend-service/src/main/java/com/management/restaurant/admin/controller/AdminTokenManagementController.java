package com.management.restaurant.admin.controller;

import com.management.restaurant.helper.SessionInfo;
import com.management.restaurant.helper.TokenStatistics;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.service.RefreshTokenService;
import com.management.restaurant.service.monitor.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller for Token Management
 * <p>
 * Endpoints: </br>
 * - View token statistics </br>
 * - Revoke tokens  </br>
 * - Monitor active sessions    </br>
 * - Security analytics </br>
 */
@RestController
@RequestMapping("/api/v1/admin/tokens")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminTokenManagementController {
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final MetricsService metricsService;

    /**
     * Get overall token statistics </br>
     * GET /api/v1/admin/tokens/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TokenStatistics> getStatistics() {
        log.info("📊 Admin requested token statistics");
        return ResponseEntity.ok(metricsService.getStatistics());
    }

    /**
     * Get user's active sessions (devices) </br>
     * GET /api/v1/admin/tokens/sessions/{userId}
     */
    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<SessionInfo>> getUserSessions(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("📱 Admin viewing sessions for user: {}", user.getUsername());
        return ResponseEntity.ok(metricsService.getUserActiveSessions(user));
    }

    /**
     * Revoke all tokens for a user </br>
     * DELETE /api/v1/admin/tokens/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> revokeAllUserTokens(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "Admin action") String reason) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int revokedCount = refreshTokenService.revokeAllUserTokens(user, reason);

        log.warn("⚠️ Admin revoked all tokens for user: {} (count: {})", user.getUsername(), revokedCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All tokens revoked for user: " + user.getUsername(),
                "revokedCount", revokedCount
        ));
    }

    /**
     * Revoke tokens from specific IP   </br>
     * DELETE /api/v1/admin/tokens/ip/{ipAddress}
     */
    @DeleteMapping("/ip/{ipAddress}")
    public ResponseEntity<Map<String, Object>> revokeTokensByIp(
            @PathVariable String ipAddress,
            @RequestParam(required = false, defaultValue = "Suspicious activity") String reason) {

        int revokedCount = refreshTokenService.revokeTokensByIp(ipAddress, reason);

        log.warn("⚠️ Admin revoked tokens from IP: {} (count: {})", ipAddress, revokedCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tokens revoked from IP: " + ipAddress,
                "revokedCount", revokedCount
        ));
    }

    /**
     * Revoke entire token family (rotation chain)  </br>
     * DELETE /api/v1/admin/tokens/family/{tokenFamily}
     */
    @DeleteMapping("/family/{tokenFamily}")
    public ResponseEntity<Map<String, Object>> revokeTokenFamily(
            @PathVariable String tokenFamily,
            @RequestParam(required = false, defaultValue = "Security breach") String reason) {

        int revokedCount = refreshTokenService.revokeTokenFamily(tokenFamily, reason);

        log.warn("⚠️ Admin revoked token family: {} (count: {})", tokenFamily, revokedCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token family revoked",
                "revokedCount", revokedCount
        ));
    }

    /**
     * Manually trigger token cleanup   </br>
     * POST /api/v1/admin/tokens/cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> triggerCleanup() {
        log.info("🧹 Admin triggered manual token cleanup");

        refreshTokenService.cleanupExpiredTokens();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token cleanup completed"
        ));
    }

    /**
     * Manually trigger token purge (hard delete)   </br>
     * POST /api/v1/admin/tokens/purge
     */
    @PostMapping("/purge")
    public ResponseEntity<Map<String, Object>> triggerPurge() {
        log.info("🗑️ Admin triggered manual token purge");

        refreshTokenService.purgeOldDeletedTokens();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Old tokens purged successfully"
        ));
    }
}