package com.management.restaurant.repository;

import com.management.restaurant.helper.SessionInfo;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Find token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all tokens for a user (including soft-deleted)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    List<RefreshToken> findAllByUserId(@Param("userId") Long userId);

    Optional<RefreshToken> findByUser_Id(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find active tokens for a user (not soft-deleted)
     */
    List<RefreshToken> findByUser(User user);
    /**
     * Find tokens by token family (for rotation tracking)
     */
    List<RefreshToken> findByTokenFamily(String tokenFamily);
    /**
     * Find tokens by IP address
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Find expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now AND rt.deletedAt IS NULL")
    List<RefreshToken> findExpiredTokens(LocalDateTime localDateTime);
    /**
     * Find tokens by device ID
     */
    List<RefreshToken> findByDeviceId(String deviceId);

    /**
     * Count expired tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiryDate < :now AND rt.deletedAt IS NULL")
    long countExpiredTokens(@Param("now") LocalDateTime now);

    List<SessionInfo> findActiveTokensByUser (User user);

    /**
     * Count active tokens (not revoked, not deleted)
     */
    long countByRevokedFalseAndDeletedAtNull();

    /**
     * Count revoked tokens
     */
    long countByRevokedTrue();

    /**
     * Find revoked tokens
     */
    List<RefreshToken> findByRevokedTrue();

    /**
     * Find valid tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
            "AND rt.revoked = false " +
            "AND rt.deletedAt IS NULL " +
            "AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Delete by token (soft delete via @SQLDelete)
     */
    void deleteByToken(String token);

    /**
     * Delete by user ID (soft delete via @SQLDelete)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.deletedAt = :now, rt.deletedBy = 'system' " +
            "WHERE rt.user.id = :userId AND rt.deletedAt IS NULL")
    void softDeleteByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Hard delete old soft-deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.deletedAt < :cutoffDate")
    int deleteOldDeletedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Hard delete by user ID (for complete cleanup)
     */
    @Modifying
    @Query(value = "DELETE FROM refresh_token WHERE user_id = :userId", nativeQuery = true)
    void hardDeleteByUserId(@Param("userId") Long userId);

    // ========================================
    // ANALYTICS QUERIES
    // ========================================

    /**
     * Find most used tokens (potential security issue)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.useCount > :threshold " +
            "AND rt.deletedAt IS NULL ORDER BY rt.useCount DESC")
    List<RefreshToken> findHighlyUsedTokens(@Param("threshold") int threshold);

    /**
     * Find tokens from multiple IPs (potential account sharing)
     */
    @Query("SELECT rt.user, COUNT(DISTINCT rt.ipAddress) as ipCount " +
            "FROM RefreshToken rt " +
            "WHERE rt.deletedAt IS NULL " +
            "GROUP BY rt.user " +
            "HAVING COUNT(DISTINCT rt.ipAddress) > :threshold")
    List<Object[]> findUsersWithMultipleIPs(@Param("threshold") int threshold);

    /**
     * Get token usage statistics by user
     */
    @Query("SELECT rt.user.username, " +
            "COUNT(rt), " +
            "SUM(rt.useCount), " +
            "AVG(rt.useCount) " +
            "FROM RefreshToken rt " +
            "WHERE rt.deletedAt IS NULL " +
            "GROUP BY rt.user.username")
    List<Object[]> getTokenStatisticsByUser();

    /**
     * Find tokens that haven't been used in X days
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE " +
            "rt.lastUsedAt < :cutoffDate " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findUnusedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find tokens created from suspicious IPs
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE " +
            "rt.ipAddress IN :suspiciousIPs " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findBySuspiciousIPs(@Param("suspiciousIPs") List<String> suspiciousIPs);

    // ========================================
    // DEVICE TRACKING QUERIES
    // ========================================

    /**
     * Count active devices for a user
     */
    @Query("SELECT COUNT(DISTINCT rt.deviceId) FROM RefreshToken rt " +
            "WHERE rt.user = :user " +
            "AND rt.revoked = false " +
            "AND rt.deletedAt IS NULL " +
            "AND rt.expiryDate > :now")
    long countActiveDevicesByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find all devices for a user
     */
    @Query("SELECT DISTINCT rt.deviceId, rt.deviceName, rt.lastUsedAt " +
            "FROM RefreshToken rt " +
            "WHERE rt.user = :user " +
            "AND rt.deletedAt IS NULL " +
            "ORDER BY rt.lastUsedAt DESC")
    List<Object[]> findUserDevices(@Param("user") User user);

    // ========================================
    // CLEANUP QUERIES
    // ========================================

    /**
     * Find tokens to cleanup (expired + soft-deleted)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE " +
            "(rt.expiryDate < :now OR rt.deletedAt < :now) " +
            "AND rt.deletedAt IS NOT NULL")
    List<RefreshToken> findTokensForCleanup(@Param("now") LocalDateTime now);

    /**
     * Count tokens pending cleanup
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE " +
            "(rt.expiryDate < :now OR rt.deletedAt < :now) " +
            "AND rt.deletedAt IS NOT NULL")
    long countTokensPendingCleanup(@Param("now") LocalDateTime now);
}