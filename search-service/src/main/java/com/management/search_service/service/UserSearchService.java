package com.management.search_service.service;

import com.management.search_service.document.UserDocument;
import com.management.search_service.events.implement.UserEvent;
import com.management.search_service.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {
    private final UserDocumentRepository userDocumentRepository;

    /**
     * Index user - evict all user caches
     */
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", allEntries = true, cacheManager = "caffeineCacheManager")
    })
    public void indexUser(UserEvent event) {
        try {
            UserDocument document = UserDocument.builder()
                    .id(event.getUserId().toString())
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .fullName(event.getFullName())
                    .email(event.getEmail())
                    .phoneNumber(event.getPhoneNumber())
                    .address(event.getAddress())
                    .roleName(event.getRoleName())
                    .status(event.getStatus())
                    .avatarUrl(event.getAvatarUrl())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userDocumentRepository.save(document);
            log.info("Indexed user document: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to index user: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete user - evict caches
     */
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", allEntries = true, cacheManager = "caffeineCacheManager")
    })
    public void deleteUser(Long userId) {
        try {
            userDocumentRepository.deleteByUserId(userId);
            log.info("Deleted user: {} - USER CACHES EVICTED", userId);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Search users - cache by query + pagination
     */
    @Cacheable(value = "users",
            key = "'search:' + (#query ?: 'all') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            cacheManager = "redisCacheManager")
    public Page<UserDocument> searchUsers(String query, Pageable pageable) {
        log.info("Searching users: query={}, page={} [CACHE MISS]", query, pageable.getPageNumber());
        if (query == null || query.trim().isEmpty()) {
            return userDocumentRepository.findAll(pageable);
        }
        return userDocumentRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                query, query, pageable);
    }

    /**
     * Find by role - cache enabled
     */
    @Cacheable(value = "users",
            key = "'role:' + #roleName + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<UserDocument> findByRole(String roleName, Pageable pageable) {
        log.info("Finding users by role: {} [CACHE MISS]", roleName);
        return userDocumentRepository.findByRoleName(roleName, pageable);
    }

    /**
     * Find by status - cache enabled
     */
    @Cacheable(value = "users",
            key = "'status:' + #status + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<UserDocument> findByStatus(String status, Pageable pageable) {
        log.info("Finding users by status: {} [CACHE MISS]", status);
        return userDocumentRepository.findByStatus(status, pageable);
    }

    /**
     * Find by ID - frequently accessed, cache individual users
     */
    @Cacheable(value = "users",
            key = "'id:' + #userId",
            cacheManager = "redisCacheManager")
    public Optional<UserDocument> findById(Long userId) {
        log.info("Finding user by ID: {} [CACHE MISS]", userId);
        return userDocumentRepository.findByUserId(userId);
    }

    /**
     * Find by username - frequently accessed for login/auth
     */
    @Cacheable(value = "users",
            key = "'username:' + #username",
            cacheManager = "redisCacheManager")
    public Optional<UserDocument> findByUsername(String username) {
        log.info("Finding user by username: {} [CACHE MISS]", username);
        return userDocumentRepository.findByUsername(username);
    }

    /**
     * Find by email - frequently accessed
     */
    @Cacheable(value = "users",
            key = "'email:' + #email",
            cacheManager = "redisCacheManager")
    public Optional<UserDocument> findByEmail(String email) {
        log.info("Finding user by email: {} [CACHE MISS]", email);
        return userDocumentRepository.findByEmail(email);
    }
}