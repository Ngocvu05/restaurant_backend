package com.management.restaurant.service.oauth2;

import com.management.restaurant.dto.oauth2.OAuth2LinkDTO;
import com.management.restaurant.model.OAuth2Link;
import com.management.restaurant.model.User;

import java.util.List;
import java.util.Map;

/**
 * Service for managing OAuth2 account links
 */
public interface OAuth2LinkService {
    /**
     * Link OAuth2 account to user
     */
    OAuth2Link linkAccount(User user, String provider, String providerUserId,
                           String providerEmail, String displayName, String pictureUrl);

    /**
     * Unlink OAuth2 account
     */
    void unlinkAccount(Long userId, String provider);

    /**
     * Get all linked providers for user
     */
    List<OAuth2LinkDTO> getLinkedProviders(Long userId);

    /**
     * Check if user has linked a provider
     */
    boolean hasLinkedProvider(Long userId, String provider);

    /**
     * Set OAuth2 link as primary
     */
    void setPrimaryProvider(Long userId, String provider);

    /**
     * Update OAuth2 link usage
     */
    void recordUsage(Long userId, String provider);

    /**
     * Update OAuth2 tokens
     */
    void updateTokens(Long userId, String provider, String accessToken,
                      String refreshToken, Long expiresIn);

    /**
     * Find user by OAuth2 credentials
     */
    User findUserByOAuth2(String provider, String providerUserId);

    /**
     * Get OAuth2 link details
     */
    OAuth2LinkDTO getLinkDetails(Long userId, String provider);

    /**
     * Cleanup expired tokens
     */
    int cleanupExpiredTokens();

    /**
     * Get OAuth2 statistics
     */
    Map<String, Long> getProviderStatistics();
}
