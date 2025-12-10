package com.management.restaurant.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Base Entity with Soft Delete support
 *
 * Entities that extend this class will:
 * 1. Never be physically deleted from database
 * 2. Be marked as "deleted" with timestamp when delete() is called
 * 3. Be automatically filtered from queries (using @Where clause)
 * 4. Track who deleted the record
 *
 * How Soft Delete works:
 * - When you call repository.delete(entity):
 *   → @SQLDelete intercepts the DELETE SQL
 *   → Converts it to UPDATE statement setting deleted_at = NOW()
 *   → Entity still exists in database but marked as deleted
 *
 * - When you call repository.findAll() or any query:
 *   → @Where(clause = "deleted_at IS NULL") automatically added to SQL
 *   → Only returns entities where deleted_at is NULL (not deleted)
 *   → Deleted entities are hidden from normal queries
 *
 * Example usage:
 *
 * @Entity
 * public class User extends SoftDeletableEntity {
 *     private String username;
 *     // ... other fields
 * }
 *
 * // When you delete:
 * userRepository.delete(user);
 * // SQL: UPDATE users SET deleted_at = NOW(), deleted_by = 'admin' WHERE id = 1
 *
 * // When you query:
 * userRepository.findAll();
 * // SQL: SELECT * FROM users WHERE deleted_at IS NULL
 */
@Getter
@Setter
@MappedSuperclass
@SQLDelete(sql = "UPDATE {h-schema}{h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SoftDeletableEntity extends BaseEntity {

    /**
     * Timestamp when entity was soft deleted
     *
     * - NULL     = Entity is active (not deleted)
     * - NOT NULL = Entity is deleted (soft deleted)
     *
     * This field is automatically set by @SQLDelete annotation
     * when repository.delete() is called
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Username of user who deleted this entity
     *
     * This is automatically populated by @SQLDelete using CURRENT_USER
     * which comes from the database session user
     *
     * Note: If you need more control over who deleted,
     * you can manually call markAsDeleted(username) instead of repository.delete()
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // ===== HELPER METHODS =====

    /**
     * Check if entity is soft deleted
     *
     * @return true if entity is deleted (deletedAt is not null)
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if entity is active (not deleted)
     *
     * @return true if entity is active (deletedAt is null)
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Manually mark entity as deleted (soft delete)
     *
     * Use this method when you want to soft delete without calling repository.delete()
     * This gives you more control over the deletion process
     *
     * Example:
     * user.markAsDeleted("admin");
     * userRepository.save(user);
     *
     * @param deletedBy username of person deleting this entity
     */
    public void markAsDeleted(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restore a soft deleted entity
     *
     * This unmarks the entity as deleted, making it active again
     *
     * Example:
     * User deletedUser = // get from database including deleted
     * deletedUser.restore();
     * userRepository.save(deletedUser);
     *
     * Note: To query deleted entities, you need native query:
     * @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
     * User findByIdIncludingDeleted(@Param("id") Long id);
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Get deletion status of this entity
     *
     * @return "DELETED" if soft deleted, "ACTIVE" if not deleted
     */
    public String getDeletionStatus() {
        return isDeleted() ? "DELETED" : "ACTIVE";
    }
}