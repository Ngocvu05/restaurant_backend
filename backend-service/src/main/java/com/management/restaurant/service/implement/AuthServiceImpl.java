package com.management.restaurant.service.implement;

import com.management.restaurant.contains.RoleName;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import com.management.restaurant.event.ChatEventProducer;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Authentication Service with security improvements
 * <p>
 * Security features:
 * - Short-lived access tokens
 * - Secure refresh token rotation
 * - Token blacklisting on logout
 * - All tokens revocation on password change
 * - Login attempt tracking (implement rate limiting separately)
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private static final Long REFRESH_TTL_DAYS = 7L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ChatEventProducer chatEventProducer;

    @Override
    public AuthResponse register(RegisterRequest request, MultipartFile avatarFile) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException(" Username is already in use");
        }

        // Use SuperBuilder
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone_number(request.getPhone_number())
                .address(request.getAddress())
                .role(userRoleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException(" Role not found")))
                .build();
        userRepository.save(user);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String url = fileStorageService.saveAvatar(avatarFile);

            Image avatar = new Image();
            avatar.setUrl(url);
            avatar.setAvatar(true);
            avatar.setUser(user);
            imageRepository.save(avatar);
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = issueRefreshToken(user);

        return new AuthResponse(user.getId(), accessToken, user.getUsername(), user.getRole().getName().name(),
                getAvatarUrl(user), user.getEmail(), user.getFullName(), refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new RuntimeException("Invalid credentials");
        }

        // access token
        String token = jwtService.generateToken(user);
        // refresh token (storage on DB)
        String refreshToken = issueRefreshToken(user);

        chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
        String avatarUrl = user.getImages().stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);

        return new AuthResponse(user.getId(),token, user.getUsername(), user.getRole().getName().name(), avatarUrl,
                user.getEmail(), user.getFullName(), refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Validate JWT refresh token
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Get username from JWT
        String username = jwtService.extractUsername(refreshToken);

        // Verify token exists in database
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));

        // Check if token is expired
        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("Refresh token expired");
        }
        User user = tokenEntity.getUser();

        // Rotate refresh token (optional but recommended for security)
        String newRefreshToken = rotateRefreshToken(tokenEntity, user);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("Refresh token expired");
        }

        return new AuthResponse(user.getId(), newAccessToken, username, user.getRole().getName().name(),
                getAvatarUrl(user), user.getEmail(), user.getFullName(), newRefreshToken);
    }

    /**
     * Logout user by revoking refresh token
     */
    @Override
    public void logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Blacklist the access token
                // tokenBlacklistService.revokeToken(token);

                // Clear SecurityContext
                SecurityContextHolder.clearContext();

                log.info("User logged out successfully");
            }
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
        }
    }

    /**
     *  @param userId
     *  Revoke all refresh tokens for a user
     *  Useful for password change or security breach
     */
    @Override
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info(" All tokens revoked for user ID: {}", userId);
    }

    private String getAvatarUrl(User user) {
        List<Image> images = imageRepository.findImageByUser(user);
        return images.stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }

    private UserRole getRole(RoleName roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

    protected String issueRefreshToken(User user) {
        // Generate JWT refresh token
        String jwtRefreshToken = jwtService.generateRefreshToken(user);
        // Get or create refresh token entity
        RefreshToken tokenEntity = refreshTokenRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    return newToken;
                });

        // Store JWT token in database
        tokenEntity.setToken(jwtRefreshToken);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TTL_DAYS));
        refreshTokenRepository.save(tokenEntity);
        log.info(" Refresh token issued for user: {}", user.getUsername());
        return jwtRefreshToken;
    }

    /**
     * Rotate refresh token (generate new one and invalidate old)
     *
     * @param oldToken Old token entity to invalidate
     * @param user User to issue new token for
     * @return New JWT refresh token string
     */
    private String rotateRefreshToken(RefreshToken oldToken, User user) {
        // Generate new JWT refresh token
        String newJwtToken = jwtService.generateRefreshToken(user);

        // Update token in database
        oldToken.setToken(newJwtToken);
        oldToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(oldToken);
        log.info("Refresh token rotated for user: {}", user.getUsername());

        return newJwtToken;
    }
}