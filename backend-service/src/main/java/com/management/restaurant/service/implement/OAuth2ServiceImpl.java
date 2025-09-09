package com.management.restaurant.service.implement;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.event.ChatEventProducer;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.OAuth2Service;
import com.management.restaurant.service.oauth2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2ServiceImpl implements OAuth2Service {

    private final OAuth2ProviderFactory providerFactory;
    private final UserOAuth2Service userService;
    private final JwtService jwtService;
    private final ChatEventProducer chatEventProducer;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse authenticateOAuth2User(OAuth2LoginRequest request) {
        String provider = request.getProvider();
        String email = null;

        try {
            log.info("OAuth2 login attempt - Provider: {}, SessionId: {}",
                    provider, request.getSessionId());

            validateRequest(request);
            rateLimitService.checkRateLimit(provider + "_" + request.getEmail());

            OAuth2Provider oauth2Provider = providerFactory.getProvider(request.getProvider());
            User user = oauth2Provider.authenticateAndGetUser(request);
            email = user.getEmail();

            String token = jwtService.generateToken(user);
            //convert chat message data
            handleChatSessionConversion(request, user);
            //build response
            AuthResponse response = buildAuthResponse(user, token);

            auditService.logOAuth2Success(provider, email);
            log.info("OAuth2 login successful for user: {}, provider: {}",
                    user.getUsername(), provider);

            return response;

        } catch (Exception e) {
            auditService.logOAuth2Failure(provider, email, e.getMessage());
            log.error("OAuth2 authentication failed for provider: {}", provider, e);
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage(), e);
        }
    }

    private void validateRequest(OAuth2LoginRequest request) {
        if (request.getProvider() == null || request.getProvider().trim().isEmpty()) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Access token is required");
        }
    }

    private void handleChatSessionConversion(OAuth2LoginRequest request, User user) {
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            return;
        }

        try {
            chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
            log.info("Chat session conversion sent for sessionId: {}, userId: {}",
                    request.getSessionId(), user.getId());
        } catch (Exception e) {
            log.error("Failed to send chat session conversion", e);
        }
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return new AuthResponse(
                user.getId(),
                token,
                user.getUsername(),
                user.getRole().getName().name(),
                userService.getAvatarUrl(user),
                user.getEmail(),
                user.getFullName(),
                issueRefreshToken(user) // refreshToken
        );
    }

    private String issueRefreshToken(User user) {
        RefreshToken rt = refreshTokenRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    RefreshToken n = new RefreshToken();
                    n.setUser(user);
                    return n;
                });

        String token = UUID.randomUUID() + "." + UUID.randomUUID();

        rt.setToken(token);
        rt.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);

        return token;
    }
}