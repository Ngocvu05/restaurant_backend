package com.management.restaurant.service;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(RegisterRequest request, MultipartFile file);

    AuthResponse login(LoginRequest request);
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refreshToken(String refreshToken);
    AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void logout(HttpServletRequest request);

    void revokeAllUserTokens(Long userId);

    void resendEmailVerification(String username);

    void revokeToken(String token, String reason);

    boolean verifyEmail(String token);

    String getAvatarUrl(User user);

    String rotateRefreshToken(RefreshToken oldToken, User user, HttpServletRequest request);

    String issueRefreshToken(User user, HttpServletRequest request);
}
