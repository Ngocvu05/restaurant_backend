package com.management.chat_service.service;

import java.util.concurrent.TimeUnit;

public interface CustomCacheService {
    /**
     * Get value from 2-tier cache
     *
     * @param cacheName Cache name (e.g., "chatRooms", "messages")
     * @param key Cache key
     * @param type Expected return type
     * @return Cached value or null if not found
     */
    <T> T get(String cacheName, String key, Class<T> type);

    /**
     * Put value to both L1 and L2 cache
     */
    <T> void put(String cacheName, String key, T value);

    /**
     * Put value with custom TTL (Redis only)
     */
    <T> void put(String cacheName, String key, T value, long timeout, TimeUnit unit);

    /**
     * Evict from both L1 and L2
     */
    void evict(String cacheName, String key);

    /**
     * Clear entire cache (both L1 and L2)
     */
    void clear(String cacheName);

    /**
     * Check if caching is enabled
     */
    boolean isCachingEnabled();

    /**
     * Get cache status for monitoring
     */
    String getCacheStatus();
}