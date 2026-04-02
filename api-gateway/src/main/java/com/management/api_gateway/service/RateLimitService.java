package com.management.api_gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    // Rate limit: 100 requests per minute per IP
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientIp) {
        String key = "rate_limit:" + clientIp;
        String currentMinute = String.valueOf(Instant.now().getEpochSecond() / 60);
        String redisKey = key + ":" + currentMinute;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                log.warn("Redis increment returned null for key: {}", redisKey);
                return true; // Fail open
            }

            if (currentCount == 1) {
                // Set expiration for the first request in this minute
                redisTemplate.expire(redisKey, WINDOW_DURATION);
            }

            boolean allowed = currentCount <= MAX_REQUESTS_PER_MINUTE;

            if (!allowed) {
                log.warn("Rate limit exceeded for IP: {} (requests: {})", clientIp, currentCount);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Error checking rate limit for IP: {}. Error: {} - {}",
                    clientIp, e.getClass().getSimpleName(), e.getMessage());

            // IMPORTANT: Fail open - allow request if Redis is down
            // This prevents Redis outage from taking down the entire system
            return true;
        }
    }

    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
}
