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
 * Daily Statistics Entity
 */
@Entity
@Table(name = "daily_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", unique = true)
    private LocalDate statDate;

    @Column(name = "total_bookings")
    private Integer totalBookings;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue;

    @Column(name = "total_customers")
    private Integer totalCustomers;

    @Column(name = "popular_dish_id")
    private Long popularDishId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    private Long version;
}
