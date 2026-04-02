package com.management.restaurant.analytics.service;

import com.management.restaurant.analytics.model.UserSession;

import java.util.List;

/**
 * User session management service in MongoDB
 * Storage: Session tokens, device info, login history
 */
public interface MongoUserSessionService {
    /**
     * Create a new session when the user logs in
     */
    UserSession createSession(Long userId, String deviceInfo,
                              String ipAddress, String userAgent);
    /**
     * Update last activity
     */
    void updateLastActivity(String sessionId);
    /**
     * Logout - mark session as inactive
     */
    public void logout(String sessionId);

    /**
     * Get the user's active sessions
     */
    List<UserSession> getActiveSessions(Long userId);

    /**
     * Logout all devices
     */
    void logoutAllSessions(Long userId);

    /**
     * Delete expired sessions
     */
    long cleanupExpiredSessions(int daysToKeep);
}