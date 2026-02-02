package com.management.restaurant.service.implement;

import com.management.restaurant.contains.RoleName;
import com.management.restaurant.contains.UserStatus;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.dto.security.UserSecurityDTO;
import com.management.restaurant.event.EventPublisherService;
import com.management.restaurant.event.model.UserEvent;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.IUserInfoMapper;
import com.management.restaurant.mapper.UserMapper;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.service.CustomCacheService;
import com.management.restaurant.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced User Service with Security & Caching
 * <p>
 * Features:
 * - L1/L2 caching for user queries
 * - Security management (lock/unlock accounts)
 * - Failed login tracking
 * - Password management
 * - Email verification
 * - Audit trail
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRoleRepository userRoleRepository;
    private final ImageRepository imageRepository;
    private final IUserInfoMapper userInfoMapper;
    private final EventPublisherService eventPublisher;

    private final PasswordEncoder passwordEncoder;
    private final CustomCacheService customCacheService;

    /**
     * Get all users with caching
     * Cache: L2 (Redis) - shared across instances
     */
    @Override
    @Cacheable(value = "users", key = "'all'", cacheManager = "redisCacheManager")
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users [CACHE MISS]");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with pagination
     */
    @Override
    @Cacheable(value = "users",
            key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize",
            cacheManager = "redisCacheManager")
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching users page {} [CACHE MISS]", pageable.getPageNumber());
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Get user by ID with caching
     * Cache: L2 (Redis) - frequently accessed
     */
    @Override
    @Cacheable(value = "users", key = "'id:' + #id",
            unless = "#result == null",
            cacheManager = "redisCacheManager")
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by ID: {} [CACHE MISS]", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    /**
     * Get user by username - frequently used for auth
     * Cache: L2 (Redis)
     */
    @Override
    @Cacheable(value = "users", key = "'username:' + #username",
            unless = "#result == null",
            cacheManager = "redisCacheManager")
    public UserDTO getUserByUsername(String username) {
        log.info("Fetching user by username: {} [CACHE MISS]", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    /**
     * Get user by email
     */
    @Override
    @Cacheable(value = "users", key = "'email:' + #email",
            unless = "#result == null",
            cacheManager = "redisCacheManager")
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {} [CACHE MISS]", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return userMapper.toDTO(user);
    }

    /**
     * Get users by role
     */
    @Cacheable(value = "users", key = "'role:' + #roleName",
            cacheManager = "redisCacheManager")
    @Override
    public List<UserDTO> getUsersByRole(String roleName) {
        log.info("Fetching users by role: {} [CACHE MISS]", roleName);

        RoleName role = RoleName.valueOf(roleName);
        return userRepository.findByRole_Name(role)
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Get active users
     */
    @Cacheable(value = "users", key = "'active'",
            cacheManager = "redisCacheManager")
    @Override
    public List<UserDTO> getActiveUsers() {
        log.info("Fetching active users [CACHE MISS]");

        UserStatus status = UserStatus.ACTIVE;
        return userRepository.findByStatusAndDeletedAtIsNull(status)
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Search users by keyword
     */
    @Override
    @Cacheable(value = "users",
            key = "'search:' + #keyword + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users with keyword: {} [CACHE MISS]", keyword);
        return userRepository.findByUsernameContainingOrFullNameContainingOrEmailContaining(
                        keyword, keyword, keyword, pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Create user - evict caches
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "'all'", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", key = "'active'", cacheManager = "redisCacheManager")
    })
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO.getUsername());
        // Validate uniqueness
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = userMapper.toEntity(userDTO);
        UserRole role = userRoleRepository.findByName(userDTO.getRoleType())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRole(role);

        // Set security defaults
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        user.setFailedLoginAttempts(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        // Publish event
        UserEvent event = createUserEvent(saved, UserEvent.Type.USER_CREATED);
        eventPublisher.publishUserEvent("user.created", event);

        log.info("✅ User created: {} - CACHES EVICTED", saved.getUsername());
        return userMapper.toDTO(saved);
    }

    /**
     * Update user - refresh specific cache and evict related caches
     */
    @Override
    @Caching(
            put = @CachePut(value = "users", key = "'id:' + #id", cacheManager = "redisCacheManager"),
            evict = {
                    @CacheEvict(value = "users", key = "'all'", cacheManager = "redisCacheManager"),
                    @CacheEvict(value = "users", key = "'username:' + #result.username", cacheManager = "redisCacheManager"),
                    @CacheEvict(value = "users", key = "'email:' + #result.email", cacheManager = "redisCacheManager", condition = "#result.email != null")
            }
    )
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user ID: {}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Update fields
        existing.setUsername(existing.getUsername());
        existing.setFullName(userDTO.getFullName());
        existing.setEmail(userDTO.getEmail());
        existing.setPhone_number(userDTO.getPhone_number());
        existing.setAddress(userDTO.getAddress());

        User updated = userRepository.save(existing);

        // Publish event
        UserEvent event = createUserEvent(updated, UserEvent.Type.USER_UPDATED);
        eventPublisher.publishUserEvent("user.updated", event);

        log.info("✅ User updated: {} - CACHES REFRESHED", updated.getUsername());
        return userMapper.toDTO(updated);
    }

    /**
     * Delete user - evict all related caches
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "'id:' + #id", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", key = "'all'", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", key = "'active'", cacheManager = "redisCacheManager")
    })
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Soft delete
        //user.softDelete("system");
        userRepository.save(user);

        // Publish event
        UserEvent event = createUserEvent(user, UserEvent.Type.USER_DELETED);
        eventPublisher.publishUserEvent("user.deleted", event);

        log.info("✅ User deleted (soft): {} - CACHES EVICTED", user.getUsername());
    }

    /**
     * Lock user account
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "'id:' + #userId", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", key = "'active'", cacheManager = "redisCacheManager")
    })
    public void lockUserAccount(Long userId, int durationMinutes, String reason) {
        log.info("Locking user account ID: {} for {} minutes", userId, durationMinutes);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.lockAccount(durationMinutes);
        userRepository.save(user);

        log.info("✅ Account locked: {} - Reason: {}", user.getUsername(), reason);
    }

    /**
     * Get user security info
     */
    @Cacheable(value = "users", key = "'security:' + #userId",
            cacheManager = "redisCacheManager")
    public UserSecurityDTO getUserSecurityInfo(Long userId) {
        log.info("Fetching security info for user ID: {} [CACHE MISS]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return UserSecurityDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .accountLockedUntil(user.getAccountLockedUntil())
                .isAccountLocked(user.isAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .emailVerified(user.getEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .passwordChangedAt(user.getPasswordChangedAt())
                .build();
    }

    /**
     * Unlock user account
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "'id:' + #userId", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "users", key = "'active'", cacheManager = "redisCacheManager")
    })
    public void unlockUserAccount(Long userId) {
        log.info("Unlocking user account ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.unlockAccount();
        userRepository.save(user);

        log.info("✅ Account unlocked: {}", user.getUsername());
    }

    /**
     * Reset failed login attempts
     */
    @Override
    @CacheEvict(value = "users", key = "'id:' + #userId", cacheManager = "redisCacheManager")
    public void resetFailedLoginAttempts(Long userId) {
        log.info("Resetting failed login attempts for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("✅ Failed login attempts reset for: {}", user.getUsername());
    }

    /**
     * Set avatar image
     */
    @Override
    @CacheEvict(value = "users", key = "'id:' + #userId", cacheManager = "redisCacheManager")
    public void setAvatarImage(Long userId, Long imageId) throws BadRequestException {
        // Lấy ảnh
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        // Check if the image belongs to the user.
        if (!image.getUser().getId().equals(userId)) {
            throw new BadRequestException("Image does not belong to the user");
        }

        // Set the current image as the avatar.
        image.setAvatar(true);
        imageRepository.save(image);

        // Assign images that are not avatars.
        imageRepository.unsetAllOtherAvatars(userId, imageId);
        log.info("✅ Avatar set for user ID: {}", userId);
    }

    /**
     * find Users by id.
     * @param userIds list ids of user.
     * @return list users' information.
     */
    @Override
    public List<UserInfoDTO> findUsersByIds(List<Long> userIds) {
        log.info("Fetching {} users by IDs", userIds.size());
        return userRepository.findAllById(userIds)
                .stream()
                .map(userInfoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users with unverified emails
     */
    @Override
    public List<UserDTO> getUsersWithUnverifiedEmails() {
        log.info("Fetching users with unverified emails");
        return userRepository.findByEmailVerifiedFalse()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get locked users
     */
    @Override
    public List<UserDTO> getLockedUsers() {
        log.info("Fetching locked users");
        return userRepository.findByAccountLockedUntilIsNotNull()
                .stream()
                .filter(User::isAccountLocked)
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ===== CACHE MANAGEMENT =====
    /**
     * Clear all user caches
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUserCache() {
        log.info("Clearing all user cache");
    }

    /**
     * Clear cache for specific user
     */
    public void clearUserCache(Long userId) {
        customCacheService.evict("users", "id:" + userId);
        log.info("✅ Cache cleared for user ID: {}", userId);
    }

    /**
     * Warm up cache with frequently accessed users
     */
    public void warmUpCache() {
        log.info("Warming up user cache...");

        // Cache active users
        getActiveUsers();

        // Cache users by common roles
        getUsersByRole("ADMIN");
        getUsersByRole("CUSTOMER");

        log.info("✅ User cache warmed up");
    }

    // ===== HELPER METHODS =====
    private UserEvent createUserEvent(User user, UserEvent.Type eventType) {
        return UserEvent.builder()
                .eventType(eventType.name())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone_number())
                .address(user.getAddress())
                .roleName(user.getRole() != null ? user.getRole().getName().name() : null)
                .status(user.getStatus().name())
                .avatarUrl(user.getImages() != null && !user.getImages().isEmpty() ?
                        user.getImages().stream()
                                .filter(Image::isAvatar)
                                .findFirst()
                                .map(Image::getUrl)
                                .orElse(null) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}