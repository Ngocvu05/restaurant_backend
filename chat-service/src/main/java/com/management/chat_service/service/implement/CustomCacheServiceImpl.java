package com.management.chat_service.service.implement;

import com.management.chat_service.service.CustomCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Custom Cache Service for Chat Service    </br>
 * Implements 2-tier caching: L1 (Caffeine) + L2 (Redis)    </br>
 * <p>
 * Flow:    </br>
 * 1. Try L1 cache (Caffeine - in-memory, ~1ms) </br>
 * 2. If miss, try L2 cache (Redis - distributed, ~10ms)    </br>
 * 3. If miss, fetch from database (~100ms) </br>
 * 4. Populate both L1 and L2
 */
@Service
@Slf4j
public class CustomCacheServiceImpl implements CustomCacheService {
    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager l2CacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Get value from 2-tier cache
     *
     * @param cacheName Cache name (e.g., "chatRooms", "messages")
     * @param key       Cache key
     * @param type      Expected return type
     * @return Cached value or null if not found
     */
    @Override
    public <T> T get(String cacheName, String key, Class<T> type) {
        // Try L1 first (Caffeine - fastest)
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            T value = l1Cache.get(key, type);
            if (value != null) {
                log.debug("✅ L1 Cache HIT: {}:{}", cacheName, key);
                return value;
            }
        }

        // Try L2 (Redis - distributed)
        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            T value = l2Cache.get(key, type);
            if (value != null) {
                log.debug("✅ L2 Cache HIT: {}:{}", cacheName, key);

                // Populate L1 for next time
                if (l1Cache != null) {
                    l1Cache.put(key, value);
                    log.debug("🔥 Populated L1 cache: {}:{}", cacheName, key);
                }
                return value;
            }
        }

        log.debug("❌ Cache MISS: {}:{}", cacheName, key);
        return null;
    }

    /**
     * Put value to both L1 and L2 cache
     *
     * @param cacheName
     * @param key
     * @param value
     */
    @Override
    public <T> void put(String cacheName, String key, T value) {
        // Put to L1 (Caffeine)
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }

        // Put to L2 (Redis)
        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.put(key, value);
        }

        log.debug("💾 Cached to L1 & L2: {}:{}", cacheName, key);
    }

    /**
     * Put value with custom TTL (Redis only)
     *
     * @param cacheName
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    @Override
    public <T> void put(String cacheName, String key, T value, long timeout, TimeUnit unit) {
        // Put to L1 (uses default TTL from config)
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }

        // Put to L2 with custom TTL
        String redisKey = "chat:" + cacheName + "::" + key;
        redisTemplate.opsForValue().set(redisKey, value, timeout, unit);

        log.debug("💾 Cached to L1 & L2 (TTL: {} {}): {}:{}", timeout, unit, cacheName, key);
    }

    /**
     * Evict from both L1 and L2
     *
     * @param cacheName
     * @param key
     */
    @Override
    public void evict(String cacheName, String key) {
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.evict(key);
        }

        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.evict(key);
        }

        log.debug("🗑️ Evicted from L1 & L2: {}:{}", cacheName, key);
    }

    /**
     * Clear entire cache (both L1 and L2)
     *
     * @param cacheName
     */
    @Override
    public void clear(String cacheName) {
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.clear();
        }

        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.clear();
        }

        log.info("🧹 Cleared cache: {}", cacheName);
    }

    /**
     * Check if caching is enabled
     */
    @Override
    public boolean isCachingEnabled() {
        return l1CacheManager != null || l2CacheManager != null;
    }

    /**
     * Get cache status for monitoring
     */
    @Override
    public String getCacheStatus() {
        if (l1CacheManager != null && l2CacheManager != null) {
            return "2-tier caching (L1 + L2)";
        } else if (l1CacheManager != null) {
            return "L1 caching only (Caffeine)";
        } else if (l2CacheManager != null) {
            return "L2 caching only (Redis)";
        } else {
            return "Caching disabled";
        }
    }
}