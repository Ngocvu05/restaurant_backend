# Search Service - Caching Strategy Guide

## 📋 Overview

Hệ thống sử dụng **2-tier caching** với Caffeine (L1) và Redis (L2) để tối ưu performance.

## 🏗️ Architecture

```
┌─────────────────┐
│   Application   │
└────────┬────────┘
         │
    ┌────▼────┐
    │   L1    │  Caffeine (In-Memory)
    │  Cache  │  TTL: 10 minutes
    └────┬────┘  Size: 1000 entries
         │
    ┌────▼────┐
    │   L2    │  Redis (Distributed)
    │  Cache  │  TTL: 1 hour
    └────┬────┘  Shared across instances
         │
    ┌────▼────┐
    │   DB    │  Elasticsearch / PostgreSQL
    └─────────┘
```

## 🎯 Caching Strategies by Service

### 1. DishSearchService

#### High-Frequency Queries (L1 Cache)
```java
// Search suggestions - Caffeine cache
@Cacheable(value = "suggestions", cacheManager = "caffeineCacheManager")
public List<Dish> searchSuggestions(String keyword)
```

**Why L1?**
- Very high frequency (autocomplete)
- Small result size
- Need ultra-fast response (<10ms)

#### Medium-Frequency Queries (L2 Cache)
```java
// Search by name - Redis cache
@Cacheable(value = "dishes", key = "'search:name:' + #keyword", 
           cacheManager = "redisCacheManager")
public List<Dish> searchByName(String keyword)
```

**Why L2?**
- Moderate frequency
- Shared across instances
- Result reusable by all users

#### Cache Eviction
```java
@Caching(evict = {
    @CacheEvict(value = "dishes", allEntries = true),
    @CacheEvict(value = "search", allEntries = true),
    @CacheEvict(value = "suggestions", allEntries = true)
})
public void indexDish(DishEvent event)
```

**Strategy:** Evict all related caches when dish is created/updated

### 2. AdvancedDishSearchService

#### Fuzzy Search
```java
@Cacheable(value = "search", 
           key = "'fuzzy:' + #keyword + ':' + #maxEdits",
           cacheManager = "redisCacheManager")
```

**Cache Key:** `fuzzy:{keyword}:{maxEdits}`
- Example: `fuzzy:pho:2`

#### Autocomplete (L1)
```java
@Cacheable(value = "suggestions", 
           key = "'autocomplete:' + #prefix",
           cacheManager = "caffeineCacheManager")
```

**Why L1?**
- Extremely high frequency (every keystroke)
- Small result size (10 items)
- Need <5ms response

#### Aggregations (L2)
```java
@Cacheable(value = "search", 
           key = "'agg:' + #keyword + ':' + #category + ':' + #minPrice + ':' + #maxPrice",
           cacheManager = "redisCacheManager")
```

**Why L2?**
- Expensive query (multiple aggregations)
- Sharable results
- Longer TTL acceptable

### 3. SearchAnalyticsService

#### Top Searches
```java
@Cacheable(value = "analytics", 
           key = "'top:' + #limit + ':' + #daysBack",
           cacheManager = "redisCacheManager")
public List<SearchAnalyticsDto> getTopSearches(int limit, int daysBack)
```

**TTL:** 1 hour (analytics don't need real-time accuracy)

#### Cache Eviction Strategy
```java
@Async
@Caching(evict = {
    @CacheEvict(value = "analytics", key = "'top:*'", allEntries = true),
    @CacheEvict(value = "analytics", key = "'trending'")
})
public void trackSearch(String keyword, ...)
```

**Strategy:** Async eviction to not block search tracking

### 4. GeospatialSearchService

#### Nearby Search
```java
@Cacheable(value = "search", 
           key = "'geo:nearby:' + #lat + ':' + #lon + ':' + #distance",
           cacheManager = "redisCacheManager")
```

**Cache Key:** `geo:nearby:{lat}:{lon}:{distance}`
- Example: `geo:nearby:10.762622:106.660172:5km`

**Note:** Coordinate precision to 6 decimal places (~0.1m accuracy)

#### Combined Geo + Text Search
```java
@Cacheable(value = "search", 
           key = "'geo:combined:' + #keyword + ':' + #lat + ':' + #lon + ':' + #distance",
           cacheManager = "redisCacheManager")
```

### 5. ReviewSearchService & UserSearchService

#### Cache Strategy
- **Write operations:** Evict all caches
- **Read operations:** Cache by query parameters
- **Individual lookups:** Cache by ID/username/email

```java
// Evict on write
@Caching(evict = {
    @CacheEvict(value = "reviews", allEntries = true, cacheManager = "redisCacheManager"),
    @CacheEvict(value = "reviews", allEntries = true, cacheManager = "caffeineCacheManager")
})

// Cache on read
@Cacheable(value = "reviews", 
           key = "'dish:' + #dishId + ':' + #pageable.pageNumber",
           cacheManager = "redisCacheManager")
```

## 🔑 Cache Key Design Patterns

### 1. Simple Search
```
Pattern: "{type}:{keyword}"
Example: "search:name:pho"
```

### 2. Filtered Search
```
Pattern: "{type}:{param1}:{param2}:..."
Example: "filter:pho:soup:null:null"
         "filter:{keyword}:{category}:{minPrice}:{maxPrice}"
```

### 3. Paginated Results
```
Pattern: "{type}:{query}:{pageNum}:{pageSize}"
Example: "page:pho:0:20"
```

### 4. Geospatial
```
Pattern: "geo:{operation}:{lat}:{lon}:{distance}"
Example: "geo:nearby:10.762622:106.660172:5km"
```

### 5. Aggregations
```
Pattern: "agg:{params...}"
Example: "agg:pho:soup:50000:100000"
```

## ⚙️ Configuration

### Caffeine (L1) - application.yml
```yaml
spring:
  cache:
    caffeine:
      initial-capacity: 100
      maximum-size: 1000
      expire-after-write: 10  # minutes
```

### Redis (L2) - application.yml
```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      database: 1  # Dedicated DB for search service
  cache:
    redis:
      time-to-live: 3600000  # 1 hour in ms
```

## 📊 Cache Performance Metrics

### Monitor These Metrics
```java
// Caffeine stats (auto-enabled with recordStats())
- Hit rate
- Miss rate
- Eviction count
- Load time

// Redis stats (via Spring Boot Actuator)
- Memory usage
- Hit/miss ratio
- Key count
```

### Access Metrics
```bash
# Actuator endpoint
GET /actuator/metrics/cache.gets
GET /actuator/metrics/cache.puts
GET /actuator/metrics/cache.evictions
```

## 🎯 Best Practices

### 1. Cache What's Expensive
✅ DO cache:
- Complex aggregations
- Multi-field searches
- Geo-distance calculations
- Frequently accessed data

❌ DON'T cache:
- Real-time data
- User-specific data (unless user-keyed)
- Write operations

### 2. Choose Right Cache Level

**L1 (Caffeine):**
- Autocomplete/suggestions
- Hot data (accessed every second)
- Small result sets (<1KB)

**L2 (Redis):**
- Complex query results
- Shared across instances
- Larger result sets
- Moderate frequency

### 3. Cache Key Design
```java
// ✅ GOOD: Specific, predictable
"search:name:pho"
"dish:category:soup:page:0"

// ❌ BAD: Too generic, collision risk
"search"
"results"
```

### 4. Eviction Strategy
```java
// ✅ GOOD: Targeted eviction
@CacheEvict(value = "dishes", key = "'id:' + #dishId")

// ⚠️ CAREFUL: Evict all (expensive but safe)
@CacheEvict(value = "dishes", allEntries = true)

// ❌ BAD: No eviction on write
// Will serve stale data!
```

## 🔄 Cache Invalidation Scenarios

### Scenario 1: Dish Updated
```
Action: Update dish price
Evict: 
  - dishes:*
  - search:*
  - suggestions:*
  
Reason: Price affects search results, filters, suggestions
```

### Scenario 2: New Review Added
```
Action: Add review
Evict:
  - reviews:*
  - dishes:id:{dishId}  (rating changed)
  
Reason: Affects dish average rating and review list
```

### Scenario 3: Analytics Data Point
```
Action: Track search
Evict:
  - analytics:top:*
  - analytics:trending
  
Method: Async eviction (don't block search)
```

## 🚀 Performance Targets

| Query Type | Target | With Cache |
|------------|--------|------------|
| Autocomplete | <50ms | <5ms (L1) |
| Simple Search | <200ms | <20ms (L2) |
| Complex Search | <500ms | <50ms (L2) |
| Aggregations | <1s | <100ms (L2) |
| Geo Search | <300ms | <30ms (L2) |

## 🛠️ Troubleshooting

### High Cache Miss Rate
```java
// Check cache key consistency
log.info("Cache key: {}", cacheKey);

// Monitor hit/miss ratio
// Expected: >70% hit rate for stable data
```

### Memory Issues
```java
// Reduce Caffeine max size
maximum-size: 500  # From 1000

// Reduce Redis TTL
time-to-live: 1800000  # 30 minutes instead of 1 hour
```

### Stale Data
```java
// Add explicit eviction
@CacheEvict in write operations

// Reduce TTL for frequently changing data
expire-after-write: 5  # minutes for volatile data
```

## 📈 Monitoring Dashboard (Grafana)

```promql
# Cache hit rate
rate(cache_gets_total{result="hit"}[5m]) 
  / 
rate(cache_gets_total[5m])

# Cache size
cache_size{cache="dishes"}

# Eviction rate
rate(cache_evictions_total[5m])
```

## 🔐 Security Considerations

1. **Cache Key Sanitization**
```java
// Prevent cache poisoning
String safeKey = keyword.replaceAll("[^a-zA-Z0-9-_]", "");
```

2. **User Data Isolation**
```java
// Include userId in cache key for user-specific data
key = "'user:' + #userId + ':favorites'"
```

3. **Sensitive Data**
```java
// DON'T cache PII or sensitive data
// ❌ @Cacheable for user passwords, payment info
```

## 📚 Additional Resources

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Caffeine GitHub](https://github.com/ben-manes/caffeine)
- [Redis Caching Best Practices](https://redis.io/docs/manual/patterns/cache/)