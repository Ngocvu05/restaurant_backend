package com.management.search_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomCacheService {
    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager l2CacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public <T> T get(String cacheName, String key, Class<T> type) {
        // Try L1 first
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            T value = l1Cache.get(key, type);
            if (value != null) {
                log.debug("L1 Cache HIT: {}:{}", cacheName, key);
                return value;
            }
        }

        // Try L2
        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            T value = l2Cache.get(key, type);
            if (value != null) {
                log.debug("L2 Cache HIT: {}:{}", cacheName, key);
                // Populate L1
                if (l1Cache != null) {
                    l1Cache.put(key, value);
                }
                return value;
            }
        }

        log.debug("Cache MISS: {}:{}", cacheName, key);
        return null;
    }

    public <T> void put(String cacheName, String key, T value) {
        // Put to both L1 and L2
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }

        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.put(key, value);
        }

        log.debug("Cached to L1 & L2: {}:{}", cacheName, key);
    }

    public void evict(String cacheName, String key) {
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.evict(key);
        }

        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.evict(key);
        }

        log.debug("Evicted from L1 & L2: {}:{}", cacheName, key);
    }
}