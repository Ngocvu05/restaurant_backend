package com.management.restaurant.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Redis Cache Manager (L2 - Distributed Cache)
     * - Shared across multiple instances
     * - TTL: 10 minutes default
     */
    @Bean(name = "redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));

        // Custom TTL for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User cache: 15 minutes
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Profile cache: 10 minutes
        cacheConfigurations.put("profiles", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Restaurant cache: 30 minutes (changes less frequently)
        cacheConfigurations.put("restaurants", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Menu items cache: 20 minutes
        cacheConfigurations.put("menu-items", defaultConfig.entryTtl(Duration.ofMinutes(20)));

        // Orders cache: 5 minutes
        cacheConfigurations.put("orders", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Caffeine Cache Manager (L1 - In-Memory Cache)
     * - Local to each instance
     * - Fast access (microseconds)
     * - TTL: 5 minutes default
     */
    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "users",
                "profiles",
                "restaurants",
                "menu-items",
                "orders",
                "categories"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)  // Max 1000 entries
                .expireAfterWrite(5, TimeUnit.MINUTES)  // TTL: 5 minutes
                .expireAfterAccess(3, TimeUnit.MINUTES)  // Remove if not accessed for 3 minutes
                .recordStats());  // Enable statistics

        return cacheManager;
    }

    /**
     * Composite Cache Manager (L1 + L2)
     * - Checks L1 first, then L2
     * - Writes to both layers
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            CacheManager caffeineCacheManager,
            CacheManager redisCacheManager) {

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager(
                caffeineCacheManager,  // L1 - checked first
                redisCacheManager      // L2 - checked if L1 miss
        );
        compositeCacheManager.setFallbackToNoOpCache(false);
        return compositeCacheManager;
    }
}