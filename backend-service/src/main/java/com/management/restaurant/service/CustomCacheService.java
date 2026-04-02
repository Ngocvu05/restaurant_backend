package com.management.restaurant.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.concurrent.TimeUnit;

public interface CustomCacheService {
    <T> T get(String cacheName, String key, Class<T> type);
    <T> void put(String cacheName, String key, T value);
    <T> void put(String cacheName, String key, T value, long ttl, TimeUnit unit);
    void evict(String cacheName, String key);
    void clear(String cacheName);
    CacheStats getL1Stats(String cacheName);
}
