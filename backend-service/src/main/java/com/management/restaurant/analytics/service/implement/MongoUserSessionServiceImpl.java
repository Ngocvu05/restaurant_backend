package com.management.restaurant.analytics.service.implement;

import com.management.restaurant.analytics.model.UserSession;
import com.management.restaurant.analytics.repository.UserSessionMongoRepository;
import com.management.restaurant.analytics.service.MongoUserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoUserSessionServiceImpl implements MongoUserSessionService {
    private final UserSessionMongoRepository sessionRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Create a new session when the user logs in
     */
    @Override
    public UserSession createSession(Long userId, String deviceInfo, String ipAddress, String userAgent) {
        UserSession session = UserSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .isActive(true)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Update last activity
     */
    @Override
    public void updateLastActivity(String sessionId) {
        Query query = new Query(Criteria.where("sessionId").is(sessionId));
        Update update = new Update()
                .set("lastActivity", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, UserSession.class);
    }

    /**
     * Logout - mark session as inactive
     */
    @Override
    public void logout(String sessionId) {
        Query query = new Query(Criteria.where("sessionId").is(sessionId));
        Update update = new Update()
                .set("isActive", false)
                .set("logoutTime", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, UserSession.class);
    }

    /**
     * Get the user's active sessions
     */
    @Override
    public List<UserSession> getActiveSessions(Long userId) {
        return sessionRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Logout all devices
     */
    @Override
    public void logoutAllSessions(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("isActive").is(true));
        Update update = new Update()
                .set("isActive", false)
                .set("logoutTime", LocalDateTime.now());

        mongoTemplate.updateMulti(query, update, UserSession.class);
        log.info("Logged out all sessions for user {}", userId);
    }

    /**
     * Delete expired sessions
     */
    @Override
    public long cleanupExpiredSessions(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        return sessionRepository.deleteByLastActivityBefore(cutoff);
    }

    /**
     * Check if the session is valid.
     */
    public boolean isSessionValid(String sessionId) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionId(sessionId);

        if (sessionOpt.isEmpty()) {
            return false;
        }

        UserSession session = sessionOpt.get();

        // Check if active và chưa expire (30 ngày)
        if (!session.isActive()) {
            return false;
        }

        LocalDateTime expireTime = session.getLastActivity().plusDays(30);
        return LocalDateTime.now().isBefore(expireTime);
    }

    /**
     * Get the user’s login history
     */
    public List<UserSession> getLoginHistory(Long userId, int limit) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .limit(limit)
                .with(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "loginTime"));

        return mongoTemplate.find(query, UserSession.class);
    }
}