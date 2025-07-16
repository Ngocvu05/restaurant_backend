package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.DashboardDTO;
import com.management.restaurant.admin.service.DashboardService;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.model.Booking;
import com.management.restaurant.service.BookingService;
import com.management.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserService userService;
    private final DashboardService dashboardService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<Booking>> getRevenueBookings(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<Booking> bookings = dashboardService.getRevenueByDate(start, end);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/status")
    public Map<String, Long> getBookingStats() {
        return dashboardService.getBookingStatusStats();
    }
}
