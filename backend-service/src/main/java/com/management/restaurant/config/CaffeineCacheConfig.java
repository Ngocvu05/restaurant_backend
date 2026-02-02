package com.management.restaurant.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache Configuration for L1 (in-memory) cache
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

    /**
     * Caffeine Cache Manager for L1 cache
     * Fast in-memory cache tier
     */
    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        log.info("Configuring Caffeine Cache Manager (L1)");
        log.info("Config - Initial: {}, Max: {}, Expire: {} minutes",
                initialCapacity, maximumSize, expireAfterWrite);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .recordStats()
        );

        // Pre-create cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "users", "roles", "dishes", "tables", "bookings"
        ));

        log.info("Caffeine Cache Manager (L1) configured successfully");
        return cacheManager;
    }
}