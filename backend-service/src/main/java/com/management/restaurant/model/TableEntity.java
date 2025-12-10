package com.management.restaurant.model;

import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.model.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tables", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_capacity", columnList = "capacity")
})
@EqualsAndHashCode(callSuper = true)
public class TableEntity extends SoftDeletableEntity {
    @Column(name = "table_name", nullable = false, unique = true, length = 50)
    private String tableName;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Business logic
    public void markAsOccupied() {
        this.status = TableStatus.OCCUPIED;
    }

    public void markAsReserved() {
        this.status = TableStatus.RESERVED;
    }

    public void markAsAvailable() {
        this.status = TableStatus.AVAILABLE;
    }

    public void markAsMaintenance() {
        this.status = TableStatus.MAINTENANCE;
    }

    public boolean isAvailable() {
        return status == TableStatus.AVAILABLE && !isDeleted();
    }
}