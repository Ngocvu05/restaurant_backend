package com.management.restaurant.repository;

import com.management.restaurant.model.OAuth2Link;
import com.management.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OAuth2 Account Links
 */
@Repository
public interface OAuth2LinkRepository extends JpaRepository<OAuth2Link, Long> {
    /**
     * Find OAuth2 link by user and provider
     */
    Optional<OAuth2Link> findByUserAndProvider(User user, String provider);

    /**
     * Find OAuth2 link by user ID and provider
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.user.id = :userId " +
            "AND o.provider = :provider AND o.deletedAt IS NULL")
    Optional<OAuth2Link> findByUserIdAndProvider(
            @Param("userId") Long userId,
            @Param("provider") String provider
    );

    /**
     * Find all OAuth2 links for a user
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.user = :user " +
            "AND o.deletedAt IS NULL ORDER BY o.linkedAt DESC")
    List<OAuth2Link> findByUser(@Param("user") User user);

    /**
     * Find all OAuth2 links for a user by ID
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.user.id = :userId " +
            "AND o.deletedAt IS NULL ORDER BY o.linkedAt DESC")
    List<OAuth2Link> findByUserId(@Param("userId") Long userId);

    /**
     * Find OAuth2 link by provider user ID
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.provider = :provider " +
            "AND o.providerUserId = :providerUserId AND o.deletedAt IS NULL")
    Optional<OAuth2Link> findByProviderAndProviderUserId(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    /**
     * Find OAuth2 link by provider email
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.provider = :provider " +
            "AND o.providerEmail = :email AND o.deletedAt IS NULL")
    List<OAuth2Link> findByProviderAndEmail(
            @Param("provider") String provider,
            @Param("email") String email
    );

    /**
     * Find primary OAuth2 link for user
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.user.id = :userId " +
            "AND o.isPrimary = true AND o.deletedAt IS NULL")
    Optional<OAuth2Link> findPrimaryLinkByUserId(@Param("userId") Long userId);

    /**
     * Check if user has linked a specific provider
     */
    @Query("SELECT COUNT(o) > 0 FROM OAuth2Link o WHERE o.user.id = :userId " +
            "AND o.provider = :provider AND o.deletedAt IS NULL")
    boolean hasLinkedProvider(
            @Param("userId") Long userId,
            @Param("provider") String provider
    );

    /**
     * Count linked providers for user
     */
    @Query("SELECT COUNT(o) FROM OAuth2Link o WHERE o.user.id = :userId " +
            "AND o.deletedAt IS NULL")
    long countLinkedProviders(@Param("userId") Long userId);

    /**
     * Find links with expired tokens
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.tokenExpiresAt < :now " +
            "AND o.deletedAt IS NULL")
    List<OAuth2Link> findLinksWithExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find links not used since specific date (for cleanup)
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.lastUsedAt < :since " +
            "AND o.deletedAt IS NULL")
    List<OAuth2Link> findInactiveLinks(@Param("since") LocalDateTime since);

    /**
     * Count users by OAuth2 provider
     */
    @Query("SELECT o.provider, COUNT(DISTINCT o.user.id) FROM OAuth2Link o " +
            "WHERE o.deletedAt IS NULL GROUP BY o.provider")
    List<Object[]> countUsersByProvider();

    /**
     * Find recently linked accounts (last N days)
     */
    @Query("SELECT o FROM OAuth2Link o WHERE o.linkedAt >= :since " +
            "AND o.deletedAt IS NULL ORDER BY o.linkedAt DESC")
    List<OAuth2Link> findRecentLinks(@Param("since") LocalDateTime since);

    //List<OAuth2Link> findDeletedBefore (LocalDateTime cutoffDate);
}