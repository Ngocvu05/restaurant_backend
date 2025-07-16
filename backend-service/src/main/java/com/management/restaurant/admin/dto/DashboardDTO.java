package com.management.restaurant.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private BigDecimal totalRevenue;
    private long totalBookings;
    private long totalUsers;
    private long totalDishes;
    private long totalTables;
}
