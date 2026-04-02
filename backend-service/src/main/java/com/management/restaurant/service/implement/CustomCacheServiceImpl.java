package com.management.restaurant.service.implement;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.management.restaurant.service.CustomCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
     * Get value from cache (L1 -> L2 -> null)
     * @param cacheName
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T get(String cacheName, String key, Class<T> type) {
        // Try L1 first (Caffeine)
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            T value = l1Cache.get(key, type);
            if (value != null) {
                log.debug("✅ L1 Cache HIT: {}:{}", cacheName, key);
                return value;
            }
        }

        // Try L2 (Redis)
        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            T value = l2Cache.get(key, type);
            if (value != null) {
                log.debug("✅ L2 Cache HIT: {}:{}", cacheName, key);

                // Populate L1 for next access
                if (l1Cache != null) {
                    l1Cache.put(key, value);
                    log.debug("📥 Populated L1 cache: {}:{}", cacheName, key);
                }
                return value;
            }
        }

        log.debug("❌ Cache MISS: {}:{}", cacheName, key);
        return null;
    }

    /**
     * @param cacheName
     * @param key
     * @param value
     * @param <T>
     * Put value to both L1 and L2 cache
     */
    @Override
    public <T> void put(String cacheName, String key, T value) {
        // Put to L1
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }

        // Put to L2
        Cache l2Cache = l2CacheManager.getCache(cacheName);
        if (l2Cache != null) {
            l2Cache.put(key, value);
        }

        log.debug("💾 Cached to L1 & L2: {}:{}", cacheName, key);
    }

    /**
     * Put with custom TTL (only for Redis)
     * @param cacheName
     * @param key
     * @param value
     * @param ttl
     * @param unit
     * @param <T>
     */
    @Override
    public <T> void put(String cacheName, String key, T value, long ttl, TimeUnit unit) {
        // Put to L1 (uses default TTL)
        Cache l1Cache = l1CacheManager.getCache(cacheName);
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }

        // Put to L2 with custom TTL
        String redisKey = cacheName + "::" + key;
        redisTemplate.opsForValue().set(redisKey, value, ttl, unit);

        log.debug("💾 Cached to L1 & L2 (TTL: {} {}): {}:{}", ttl, unit, cacheName, key);
    }

    /**
     * Evict from both L1 and L2
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
     * Clear entire cache
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
     * Get L1 cache statistics
     * @param cacheName
     * @return
     */
    @Override
    public CacheStats getL1Stats(String cacheName) {
        Cache cache = l1CacheManager.getCache(cacheName);
        if (cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) cache;
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    caffeineCache.getNativeCache();
            return nativeCache.stats();
        }
        return null;
    }
}