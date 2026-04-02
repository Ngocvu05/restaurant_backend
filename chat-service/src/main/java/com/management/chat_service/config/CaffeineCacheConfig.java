package com.management.chat_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache Configuration for Chat Service
 * L1 (in-memory) cache for fast local access   </br>
 * <p>
 * Cache Names: </br>
 * - chatRooms: Chat room entities  </br>
 * - messages: Chat messages (paginated)    </br>
 * - userInfo: User info from user-service </br>
 * - aiResponses: AI generated responses
 */
@Slf4j
@Configuration
public class CaffeineCacheConfig {
    @Value("${spring.cache.caffeine.initial-capacity:100}")
    private int initialCapacity;

    @Value("${spring.cache.caffeine.maximum-size:1000}")
    private long maximumSize;

    @Value("${spring.cache.caffeine.expire-after-write:10}")
    private long expireAfterWrite;

    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        log.info("🔧 Configuring Caffeine Cache Manager (L1) for Chat Service");
        log.info("Config - Initial: {}, Max: {}, Expire: {} minutes",
                initialCapacity, maximumSize, expireAfterWrite);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .recordStats()
        );

        // Pre-create cache names for chat service
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "chatRooms",      // Chat room entities (by ID, roomId, userId)
                "messages",       // Chat messages (paginated by roomId)
                "userInfo",       // User info from user-service API
                "aiResponses",    // AI generated responses (for deduplication)
                "participants"    // Chat participants info
        ));

        log.info("✅ Caffeine Cache Manager configured successfully");
        return cacheManager;
    }
}