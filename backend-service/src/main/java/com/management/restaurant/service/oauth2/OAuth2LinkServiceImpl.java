package com.management.restaurant.service.oauth2;

import com.management.restaurant.dto.oauth2.OAuth2LinkDTO;
import com.management.restaurant.model.OAuth2Link;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.OAuth2LinkRepository;
import com.management.restaurant.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced OAuth2 Link Service Implementation
 * with proper soft delete support
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuth2LinkServiceImpl implements OAuth2LinkService {
    /**
     * Link OAuth2 account to user
     * Implementation of OAuth2 Link Service
     * <p> @param user </p>
     * <p> @param provider </p>
     * <p> @param providerUserId </p>
     * <p> @param providerEmail </p>
     * <p> @param displayName </p>
     * <p> @param pictureUrl </p>
     */
    private final OAuth2LinkRepository oauth2LinkRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /**
     * Link OAuth2 account to user
     */
    @Override
    public OAuth2Link linkAccount(User user, String provider, String providerUserId, String providerEmail, String displayName, String pictureUrl) {
        log.info("Linking OAuth2 account - User: {}, Provider: {}",
                user.getUsername(), provider);

        // Check if already linked
        Optional<OAuth2Link> existing = oauth2LinkRepository.findByUserAndProvider(user, provider);
        if (existing.isPresent()) {
            // Update existing link
            OAuth2Link link = existing.get();
            link.setProviderUserId(providerUserId);
            link.setProviderEmail(providerEmail);
            link.setProviderDisplayName(displayName);
            link.setProviderPictureUrl(pictureUrl);
            link.recordUsage();

            log.info("Updated existing OAuth2 link - User: {}, Provider: {}",
                    user.getUsername(), provider);
            return oauth2LinkRepository.save(link);
        }

        // Create new link
        OAuth2Link newLink = OAuth2Link.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerEmail(providerEmail)
                .providerDisplayName(displayName)
                .providerPictureUrl(pictureUrl)
                .linkedAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .isPrimary(false)
                .build();

        // If this is the first OAuth2 link, make it primary
        long linkedCount = oauth2LinkRepository.countLinkedProviders(user.getId());
        if (linkedCount == 0) {
            newLink.setAsPrimary();
            log.info("Setting as primary OAuth2 provider (first link)");
        }

        OAuth2Link saved = oauth2LinkRepository.save(newLink);
        log.info("✅ OAuth2 account linked - User: {}, Provider: {}",
                user.getUsername(), provider);

        return saved;
    }

    /**
     * Unlink OAuth2 account
     *
     * @param userId
     * @param provider
     */
    @Override
    public void unlinkAccount(Long userId, String provider) {
        log.info("Unlinking OAuth2 account - UserId: {}, Provider: {}", userId, provider);

        OAuth2Link link = oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new RuntimeException("OAuth2 link not found"));

        // Prevent unlinking if it's the only auth method and user has no password
        User user = link.getUser();
        long linkedCount = oauth2LinkRepository.countLinkedProviders(userId);

        if (linkedCount == 1 && (user.getPassword() == null || user.getPassword().isEmpty())) {
            throw new RuntimeException(
                    "Cannot unlink the only authentication method. Please set a password first.");
        }

        // Soft delete
        link.softDelete(user.getUsername());
        oauth2LinkRepository.save(link);

        // Audit log
        auditService.logOAuth2Unlink(userId, provider);

        log.info("✅ OAuth2 account unlinked (soft deleted) - UserId: {}, Provider: {}",
                userId, provider);
    }

    /**
     * Get all linked providers for user
     *
     * @param userId
     */
    @Override
    public List<OAuth2LinkDTO> getLinkedProviders(Long userId) {
        log.debug("Getting linked providers for user: {}", userId);

        return oauth2LinkRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has linked a provider
     *
     * @param userId
     * @param provider
     */
    @Override
    public boolean hasLinkedProvider(Long userId, String provider) {
        return oauth2LinkRepository.hasLinkedProvider(userId, provider);
    }

    /**
     * Set OAuth2 link as primary
     *
     * @param userId
     * @param provider
     */
    @Override
    public void setPrimaryProvider(Long userId, String provider) {
        log.info("Setting primary OAuth2 provider - UserId: {}, Provider: {}", userId, provider);

        // Remove primary from all providers
        List<OAuth2Link> allLinks = oauth2LinkRepository.findByUserId(userId);
        String oldPrimary = null;

        for (OAuth2Link link : allLinks) {
            if (link.isPrimaryProvider()) {
                oldPrimary = link.getProvider();
            }
            link.removeAsPrimary();
        }
        oauth2LinkRepository.saveAll(allLinks);

        // Set new primary
        OAuth2Link link = oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new RuntimeException("OAuth2 link not found"));

        link.setAsPrimary();
        oauth2LinkRepository.save(link);

        // Audit log
        if (oldPrimary != null) {
            auditService.logOAuth2PrimaryChange(userId, oldPrimary, provider);
        }

        log.info("✅ Primary provider set - UserId: {}, Provider: {}", userId, provider);
    }

    /**
     * Update OAuth2 link usage
     *
     * @param userId
     * @param provider
     */
    @Override
    public void recordUsage(Long userId, String provider) {
        oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .ifPresent(link -> {
                    link.recordUsage();
                    oauth2LinkRepository.save(link);
                    log.debug("Recorded OAuth2 usage - UserId: {}, Provider: {}", userId, provider);
                });
    }

    /**
     * Update OAuth2 tokens
     *
     * @param userId
     * @param provider
     * @param accessToken
     * @param refreshToken
     * @param expiresIn
     */
    @Override
    public void updateTokens(Long userId, String provider, String accessToken, String refreshToken, Long expiresIn) {
        oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .ifPresent(link -> {
                    LocalDateTime expiresAt = expiresIn != null
                            ? LocalDateTime.now().plusSeconds(expiresIn)
                            : null;

                    link.updateToken(accessToken, refreshToken, expiresAt);
                    oauth2LinkRepository.save(link);

                    auditService.logOAuth2TokenRefresh(userId, provider);
                    log.debug("Updated OAuth2 tokens - UserId: {}, Provider: {}", userId, provider);
                });
    }

    /**
     * Find user by OAuth2 credentials
     *
     * @param provider
     * @param providerUserId
     */
    @Override
    public User findUserByOAuth2(String provider, String providerUserId) {
        return oauth2LinkRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(OAuth2Link::getUser)
                .orElse(null);
    }

    /**
     * Get OAuth2 link details
     *
     * @param userId
     * @param provider
     */
    @Override
    public OAuth2LinkDTO getLinkDetails(Long userId, String provider) {
        return oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Cleanup expired tokens
     */
    @Override
    public int cleanupExpiredTokens() {
        log.info("Cleaning up expired OAuth2 tokens");

        List<OAuth2Link> expiredLinks = oauth2LinkRepository
                .findLinksWithExpiredTokens(LocalDateTime.now());

        expiredLinks.forEach(link -> {
            link.setAccessToken(null);
            link.setRefreshToken(null);
        });

        oauth2LinkRepository.saveAll(expiredLinks);

        log.info("✅ Cleaned up {} expired OAuth2 tokens", expiredLinks.size());
        return expiredLinks.size();
    }

    /**
     * Get OAuth2 statistics
     */
    @Override
    public Map<String, Long> getProviderStatistics() {
        List<Object[]> stats = oauth2LinkRepository.countUsersByProvider();

        return stats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Hard delete old soft-deleted links (cleanup job)
     * For links soft-deleted more than X days ago
     */
    public int hardDeleteOldLinks(int daysOld) {
        log.info("Hard deleting OAuth2 links soft-deleted more than {} days ago", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        // TODO: Add query to find old soft-deleted links
//        List<OAuth2Link> oldLinks = oauth2LinkRepository
//             .findDeletedBefore(cutoffDate);
//         oauth2LinkRepository.deleteAll(oldLinks);

        log.info("✅ Hard deleted old OAuth2 links");
        return 0; // Placeholder
    }

    /**
     * Restore a soft-deleted OAuth2 link
     */
    public void restoreLink(Long userId, String provider) {
        log.info("Restoring soft-deleted OAuth2 link - UserId: {}, Provider: {}",
                userId, provider);

        // Need to query including deleted
        // TODO: Add native query to repository
        OAuth2Link link = oauth2LinkRepository.findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new RuntimeException("OAuth2 link not found"));

        if (!link.isDeleted()) {
            throw new RuntimeException("Link is not deleted");
        }

        link.restore();
        oauth2LinkRepository.save(link);

        log.info("✅ OAuth2 link restored - UserId: {}, Provider: {}", userId, provider);
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Reassign primary provider when current primary is being unlinked
     */
    private void reassignPrimaryProvider(Long userId, String providerToUnlink) {
        List<OAuth2Link> activeLinks = oauth2LinkRepository.findByUserId(userId).stream()
                .filter(link -> !link.getProvider().equals(providerToUnlink))
                .filter(link -> !link.isDeleted())
                .toList();

        if (!activeLinks.isEmpty()) {
            // Set the most recently used link as primary
            OAuth2Link newPrimary = activeLinks.stream()
                    .max((a, b) -> {
                        LocalDateTime aUsed = a.getLastUsedAt() != null ?
                                a.getLastUsedAt() : a.getLinkedAt();
                        LocalDateTime bUsed = b.getLastUsedAt() != null ?
                                b.getLastUsedAt() : b.getLinkedAt();
                        return aUsed.compareTo(bUsed);
                    })
                    .orElse(activeLinks.get(0));

            newPrimary.setAsPrimary();
            oauth2LinkRepository.save(newPrimary);

            log.info("Reassigned primary provider to: {}", newPrimary.getProvider());
        }
    }

    private OAuth2LinkDTO toDTO(OAuth2Link link) {
        return OAuth2LinkDTO.builder()
                .id(link.getId())
                .provider(link.getProvider())
                .providerEmail(link.getProviderEmail())
                .providerDisplayName(link.getProviderDisplayName())
                .providerPictureUrl(link.getProviderPictureUrl())
                .linkedAt(link.getLinkedAt())
                .lastUsedAt(link.getLastUsedAt())
                .isPrimary(link.isPrimaryProvider())
                .build();
    }
}