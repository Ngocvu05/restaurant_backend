package com.management.restaurant.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sales Report Entity - Analytics Database
 * Stores sales data for reporting and analytics
 */
@Entity
@Table(name = "sales_report", indexes = {
        @Index(name = "idx_booking_id", columnList = "booking_id"),
        @Index(name = "idx_report_date", columnList = "report_date"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to booking ID in restaurant database
     * This is NOT a foreign key - just a reference
     */
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    /**
     * Total amount of the sale
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Date of the report
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /**
     * Status of the sale: COMPLETED, CANCELLED, REFUNDED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Additional metadata (JSON format)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Created timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}