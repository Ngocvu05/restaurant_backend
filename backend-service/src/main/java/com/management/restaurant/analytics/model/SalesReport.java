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
        * Sales Report Entity - Stored in Analytics Database
 */
@Entity
@Table(name = "sales_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id")
    private Long bookingId;

    private BigDecimal amount;

    @Column(name = "report_date")
    private LocalDate reportDate;

    private String status;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
