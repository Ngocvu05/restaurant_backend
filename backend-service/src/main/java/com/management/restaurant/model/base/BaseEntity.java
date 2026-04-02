package com.management.restaurant.model.base;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base Entity with automatic auditing support
 * <p>
 * All entities should extend this class to get:
 * <p>- Automatic ID generation (Long type with auto increment)</p>
 * <p>- Created date tracking (when entity was first persisted)</p>
 * <p>- Created by tracking (username who created the entity)</p>
 * <p>- Updated date tracking (when entity was last modified)</p>
 * <p>- Updated by tracking (username who last modified the entity)</p>
 * <p>
 * How auditing works:
 * <p>1. @EntityListeners(AuditingEntityListener.class) registers JPA listener</p>
 * <p>2. When entity is saved, JPA triggers the listener</p>
 * <p>3. Listener calls AuditorAwareImpl to get current username</p>
 * <p>4. Automatically populates @CreatedBy and @LastModifiedBy fields</p>
 * <p>5. Automatically populates @CreatedDate and @LastModifiedDate fields</p>
 * <p>
 * Example usage:
 *
 * @Entity
 * public class Product extends BaseEntity {
 * <p>   private String name;</p>
 * <p>   private BigDecimal price;</p>
 * <p>   No need to add id, createdAt, createdBy, etc.</p>
 * <p>   They are inherited from BaseEntity</p>
 * }
 * <p>
 * When you save:
 * Product product = new Product();
 * product.setName("iPhone");
 * productRepository.save(product);
 * <p>
 * Automatically populated:
 * product.id = 1 (auto generated)
 * product.createdAt = 2025-01-01 10:00:00
 * product.createdBy = "admin" (from SecurityContext)
 * product.updatedAt = null (not updated yet)
 * product.updatedBy = null
 * <p>
 * When you update:
 * product.setPrice(999.99);
 * productRepository.save(product);
 * <p>
 * Automatically updated:
 * product.updatedAt = 2025-01-01 11:00:00
 * product.updatedBy = "admin" (current user)
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder  // Support inheritance
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    /**
     * Primary key for all entities
     * <p>
     * - Type: Long (supports large number of records)
     * - Generation: IDENTITY (auto increment by database)
     * - All tables will have 'id' as primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Automatically set when entity is first persisted
     * <p>
     * Managed by Spring Data JPA Auditing (@CreatedDate)
     * Cannot be updated after creation (updatable = false)
     * <p>
     * Database column: created_at
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Username of user who created this entity
     * <p>
     * Automatically populated from SecurityContext by AuditorAwareImpl
     * Cannot be updated after creation (updatable = false)
     * <p>
     * Examples:
     * - "admin" (if admin created the entity)
     * - "john_doe" (if john_doe created the entity)
     * - "system" (if created by system/background job)
     * <p>
     * Database column: created_by
     */
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    /**
     * Automatically updated when entity is modified
     * <p>
     * Managed by Spring Data JPA Auditing (@LastModifiedDate)
     * Updated every time entity is saved
     * <p>
     * Database column: updated_at
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Username of user who last modified this entity
     * <p>
     * Automatically populated from SecurityContext by AuditorAwareImpl
     * Updated every time entity is saved
     * <p>
     * Database column: updated_by
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ===== LIFECYCLE HOOKS =====

    /**
     * JPA callback method called before persisting new entity
     * <p>
     * This is a safety net in case JPA Auditing fails
     * Ensures createdAt is always set, even if auditing doesn't work
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * JPA callback method called before updating existing entity
     * <p>
     * This is a safety net in case JPA Auditing fails
     * Ensures updatedAt is always set on updates
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== HELPER METHODS =====

    /**
     * Check if this is a new entity (not yet persisted)
     *
     * @return true if entity has no ID (not saved to database yet)
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Check if this entity has been persisted to database
     *
     * @return true if entity has an ID (saved to database)
     */
    public boolean isPersisted() {
        return id != null;
    }

    /**
     * Get a string representation for logging
     * Useful for debugging
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}