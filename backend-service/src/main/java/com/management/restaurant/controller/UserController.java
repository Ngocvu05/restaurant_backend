package com.management.restaurant.controller;

import com.management.restaurant.admin.service.AdminUserService;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.dto.security.*;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.security.UserPrincipal;
import com.management.restaurant.service.AuthService;
import com.management.restaurant.service.EmailService;
import com.management.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User CRUD and security operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final EmailService emailService;
    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all users (admin only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users with pagination")
    public ResponseEntity<Page<UserDTO>> getUsersPage(Pageable pageable) {
        log.info("GET /api/users/page - Page: {}", pageable.getPageNumber());
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @UserSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by username")
    public ResponseEntity<UserDTO> getByUserName(@PathVariable String username) {
        log.info("GET /api/users/username/{}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName) {
        log.info("GET /api/users/role/{}", roleName);
        return ResponseEntity.ok(userService.getUsersByRole(roleName));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active users")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("GET /api/users/active");
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users by keyword")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam String keyword,
            Pageable pageable) {
        log.info("GET /api/users/search?keyword={}", keyword);
        return ResponseEntity.ok(userService.searchUsers(keyword, pageable));
    }

    // ===== WRITE OPERATIONS =====
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user (admin only)")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("POST /api/users - Creating user: {}", userDTO.getUsername());
        UserDTO created = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateUser(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(principal.getId(), userDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("PUT /api/users/{} - Updating user", id);
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println(">>> Token received in user-service: " + token);
        UserDTO dto = userService.getUserById(principal.getId());
        if (dto == null) {
            throw new NotFoundException("User not found");
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/avatar/{imageId}")
    public ResponseEntity<String> setAvatar(@PathVariable Long imageId, @AuthenticationPrincipal UserPrincipal principal) throws BadRequestException {
        userService.setAvatarImage(principal.getId(), imageId);
        return ResponseEntity.ok("Avatar updated successfully.");
    }

    @DeleteMapping("/delete{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (soft delete)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ===== SECURITY OPERATIONS =====

    @GetMapping("/{id}/security")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user security information")
    public ResponseEntity<UserSecurityDTO> getUserSecurityInfo(@PathVariable Long id) {
        log.info("GET /api/users/{}/security", id);
        return ResponseEntity.ok(userService.getUserSecurityInfo(id));
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock user account")
    public ResponseEntity<SecurityActionResponse> lockAccount(
            @PathVariable Long id,
            @RequestBody LockAccountRequest request) {
        log.info("POST /api/users/{}/lock - Duration: {} minutes",
                id, request.getDurationMinutes());

        userService.lockUserAccount(id, request.getDurationMinutes(), request.getReason());
        authService.revokeAllUserTokens(id);

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Account locked successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<SecurityActionResponse> unlockAccount(@PathVariable Long id) {
        log.info("POST /api/users/{}/unlock", id);

        userService.unlockUserAccount(id);

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Account unlocked successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{id}/reset-failed-attempts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset failed login attempts")
    public ResponseEntity<SecurityActionResponse> resetFailedAttempts(@PathVariable Long id) {
        log.info("POST /api/users/{}/reset-failed-attempts", id);

        userService.resetFailedLoginAttempts(id);

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Failed login attempts reset")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/unverified-emails")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users with unverified emails")
    public ResponseEntity<List<UserDTO>> getUsersWithUnverifiedEmails() {
        log.info("GET /api/users/unverified-emails");
        return ResponseEntity.ok(userService.getUsersWithUnverifiedEmails());
    }

    @GetMapping("/locked")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get locked users")
    public ResponseEntity<List<UserDTO>> getLockedUsers() {
        log.info("GET /api/users/locked");
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    // ===== PASSWORD MANAGEMENT =====

    @PostMapping("/change-password")
    @Operation(summary = "Change password (authenticated user)")
    public ResponseEntity<SecurityActionResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("POST /api/users/change-password - User: {}", username);

        UserDTO user = userService.getUserByUsername(username);
        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Password changed successfully. Please login again.")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<SecurityActionResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/users/forgot-password - Email: {}", request.getEmail());

        authService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Password reset email sent (if account exists)")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<SecurityActionResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/users/reset-password - Token provided");

        authService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Password reset successfully. Please login with new password.")
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ===== EMAIL VERIFICATION =====

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with token")
    public ResponseEntity<SecurityActionResponse> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        log.info("POST /api/users/verify-email");

        boolean verified = authService.verifyEmail(request.getToken());

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(verified)
                .message(verified ? "Email verified successfully" : "Invalid or expired token")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    public ResponseEntity<SecurityActionResponse> resendVerification(
            Authentication authentication) {
        String username = authentication.getName();
        log.info("POST /api/users/resend-verification - User: {}", username);

        authService.resendEmailVerification(username);

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Verification email sent")
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ===== CACHE MANAGEMENT =====

    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all user cache")
    public ResponseEntity<SecurityActionResponse> clearCache() {
        log.info("POST /api/users/cache/clear");

        userService.clearAllUserCache();

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("User cache cleared")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/cache/warm-up")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Warm up user cache")
    public ResponseEntity<SecurityActionResponse> warmUpCache() {
        log.info("POST /api/users/cache/warm-up");

        userService.warmUpCache();

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("User cache warmed up")
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ===== AVATAR MANAGEMENT =====

    @PutMapping("/{userId}/avatar/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#userId, authentication)")
    @Operation(summary = "Set user avatar")
    public ResponseEntity<SecurityActionResponse> setAvatar(
            @PathVariable Long userId,
            @PathVariable Long imageId) throws Exception {
        log.info("PUT /api/users/{}/avatar/{}", userId, imageId);

        userService.setAvatarImage(userId, imageId);

        return ResponseEntity.ok(SecurityActionResponse.builder()
                .success(true)
                .message("Avatar updated successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * API to get a list of users by their IDs.
     * @param ids list of user IDs to retrieve.
     *           IDs should be passed as a comma-separated string in the request parameter.
     *           If the list is empty or null, it returns a 400 Bad Request response
     * Example: /batch?ids=1,2,3
     * @return ResponseEntity list UserDTO.
     */
    @GetMapping("/batch")
    public ResponseEntity<List<UserInfoDTO>> getUsersByIds(
            @RequestParam("ids") List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<UserInfoDTO> users = userService.findUsersByIds(ids);
        log.info(">>> getUsersByIds - Fetched {} users for IDs: {}", users.size(), users);
        return ResponseEntity.ok(users);
    }
}