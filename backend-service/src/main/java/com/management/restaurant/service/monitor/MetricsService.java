package com.management.restaurant.service.monitor;

import com.management.restaurant.helper.SessionInfo;
import com.management.restaurant.helper.TokenStatistics;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.service.RefreshTokenService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    /**
     * Record booking creation time
     */
    public void recordBookingCreation(long durationMs) {
        Timer.builder("booking.creation")
                .description("Time to create a booking")
                .tag("operation", "create")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Count optimistic lock failures
     */
    public void recordOptimisticLockFailure(String entityType) {
        Counter.builder("lock.optimistic.failure")
                .description("Optimistic lock failures")
                .tag("entity", entityType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Count deadlocks
     */
    public void recordDeadlock(String operation) {
        Counter.builder("transaction.deadlock")
                .description("Deadlock occurrences")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record transaction duration
     */
    public void recordTransactionDuration(String type, long durationMs) {
        Timer.builder("transaction.duration")
                .description("Transaction execution time")
                .tag("type", type)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record lock acquisition time
     */
    public void recordLockAcquisition(String lockType, long waitTimeMs) {
        Timer.builder("lock.acquisition")
                .description("Time to acquire lock")
                .tag("type", lockType)
                .register(meterRegistry)
                .record(waitTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Count successful/failed payments
     */
    public void recordPaymentResult(boolean success) {
        Counter.builder("payment.result")
                .description("Payment processing results")
                .tag("status", success ? "success" : "failed")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Get token statistics for monitoring
     */
    public TokenStatistics getStatistics() {
        long totalTokens = refreshTokenRepository.count();
        long activeTokens = refreshTokenRepository.countByRevokedFalseAndDeletedAtNull();
        long expiredTokens = refreshTokenRepository.countExpiredTokens(LocalDateTime.now());
        long revokedTokens = refreshTokenRepository.countByRevokedTrue();

        return TokenStatistics.builder()
                .totalTokens(totalTokens)
                .activeTokens(activeTokens)
                .expiredTokens(expiredTokens)
                .revokedTokens(revokedTokens)
                .build();
    }

    /**
     * Get user's active sessions (devices)
     */
    public List<SessionInfo> getUserActiveSessions(User user) {
        return refreshTokenService.findActiveTokensByUser(user).stream()
                .map(token -> SessionInfo.builder()
                        .deviceId(token.getDeviceId())
                        .deviceName(token.getDeviceName())
                        .ipAddress(token.getIpAddress())
                        .lastUsedAt(token.getLastUsedAt())
                        .createdAt(token.getCreatedAt())
                        .useCount(token.getUseCount())
                        .build())
                .toList();
    }
}