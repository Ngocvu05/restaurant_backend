package com.management.restaurant.service.oauth2;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 10;
    private static final long TIME_WINDOW = 300000; // 5 minutes

    public void checkRateLimit(String identifier) {
        List<Long> attempts = loginAttempts.computeIfAbsent(identifier, k -> new ArrayList<>());
        long now = System.currentTimeMillis();

        attempts.removeIf(time -> now - time > TIME_WINDOW);

        if (attempts.size() >= MAX_ATTEMPTS) {
            throw new RuntimeException("Too many login attempts. Please try again later.");
        }

        attempts.add(now);
    }
}
