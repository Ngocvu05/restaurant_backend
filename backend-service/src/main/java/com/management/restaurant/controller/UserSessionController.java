package com.management.restaurant.controller;

import com.management.restaurant.helper.SessionInfo;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.security.UserPrincipal;
import com.management.restaurant.service.RefreshTokenService;
import com.management.restaurant.service.monitor.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Session Management Controller
 * <p>
 * Allows users to: </br>
 * - View their active sessions (devices)   </br>
 * - Revoke sessions from other devices </br>
 * - Logout from all devices    </br>
 */
@RestController
@RequestMapping("/api/v1/users/sessions")
@RequiredArgsConstructor
@Slf4j
public class UserSessionController {
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final MetricsService metricsService;

    /**
     * Get current user's active sessions   </br>
     * GET /api/v1/users/sessions
     */
    @GetMapping
    public ResponseEntity<List<SessionInfo>> getMySessions(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionInfo> sessions = metricsService.getUserActiveSessions(user);

        log.info("📱 User {} viewing their sessions (count: {})",
                user.getUsername(), sessions.size());

        return ResponseEntity.ok(sessions);
    }

    /**
     * Logout from all other devices (keep current session) </br>
     * POST /api/v1/users/sessions/revoke-others
     */
    @PostMapping("/revoke-others")
    public ResponseEntity<Map<String, Object>> revokeOtherDevices(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestHeader(value = "X-Device-Id", required = false) String currentDeviceId) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentDeviceId == null || currentDeviceId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Device ID is required"
            ));
        }

        int revokedCount = refreshTokenService.revokeOtherDevices(user, currentDeviceId);

        log.info("✅ User {} revoked {} sessions from other devices",
                user.getUsername(), revokedCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out from other devices",
                "revokedCount", revokedCount
        ));
    }

    /**
     * Logout from all devices (including current)  </br>
     * POST /api/v1/users/sessions/revoke-all
     */
    @PostMapping("/revoke-all")
    public ResponseEntity<Map<String, Object>> revokeAllDevices(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int revokedCount = refreshTokenService.revokeAllUserTokens(
                user,
                "User requested logout from all devices"
        );

        log.info("✅ User {} logged out from all devices (count: {})",
                user.getUsername(), revokedCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out from all devices",
                "revokedCount", revokedCount
        ));
    }

    /**
     * Get session statistics   </br>
     * GET /api/v1/users/sessions/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionInfo> activeSessions = metricsService.getUserActiveSessions(user);

        long uniqueDevices = activeSessions.stream()
                .map(SessionInfo::getDeviceId)
                .distinct()
                .count();

        long uniqueIPs = activeSessions.stream()
                .map(SessionInfo::getIpAddress)
                .distinct()
                .count();

        return ResponseEntity.ok(Map.of(
                "totalSessions", activeSessions.size(),
                "uniqueDevices", uniqueDevices,
                "uniqueIPs", uniqueIPs,
                "sessions", activeSessions
        ));
    }
}