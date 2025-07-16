package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.DashboardDTO;
import com.management.restaurant.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardDTO getDashboardSummary();

    List<Booking> getRevenueByDate(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getBookingStatusStats();
}
