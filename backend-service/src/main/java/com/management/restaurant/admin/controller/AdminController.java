package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.BookingResponseDTO;
import com.management.restaurant.admin.dto.DashboardDTO;
import com.management.restaurant.admin.dto.RevenueDTO;
import com.management.restaurant.admin.service.DashboardService;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.model.Booking;
import com.management.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {
    private final UserService userService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboardData() {
        try {
            DashboardDTO dashboard = dashboardService.getDashboardSummary();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error getting dashboard data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users != null ? users : new ArrayList<>());
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Endpoint cũ - trả về DTO thay vì Entity để tránh circular reference
    @GetMapping("/revenue")
    public ResponseEntity<List<BookingResponseDTO>> getRevenueBookings(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            log.info("Getting revenue data from {} to {}", start, end);

            List<Booking> bookings = dashboardService.getRevenueByDate(start, end);

            // Convert to DTO để tránh circular reference
            List<BookingResponseDTO> bookingDTOs = bookings.stream()
                    .map(this::convertToBookingResponseDTO)
                    .collect(Collectors.toList());

            log.info("Found {} bookings for revenue calculation", bookingDTOs.size());
            return ResponseEntity.ok(bookingDTOs);

        } catch (Exception e) {
            log.error("Error getting revenue bookings from {} to {}", start, end, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Endpoint mới - trả về dữ liệu đã group theo ngày
    @GetMapping("/revenue-grouped")
    public ResponseEntity<List<RevenueDTO>> getRevenueGroupedByDate(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            log.info("Getting grouped revenue data from {} to {}", start, end);

            List<RevenueDTO> revenueData = dashboardService.getRevenueGroupedByDate(start, end);

            if (revenueData == null) {
                revenueData = new ArrayList<>();
            }

            log.info("Found {} revenue entries grouped by date", revenueData.size());
            return ResponseEntity.ok(revenueData);

        } catch (Exception e) {
            log.error("Error getting grouped revenue data from {} to {}", start, end, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/bookings/status")
    public ResponseEntity<Map<String, Long>> getBookingStats() {
        try {
            Map<String, Long> stats = dashboardService.getBookingStatusStats();

            if (stats == null) {
                stats = new HashMap<>();
            }

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting booking status stats", e);
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setId(booking.getId());
        bookingResponseDTO.setBookingDate(booking.getBookingTime());
        bookingResponseDTO.setNote(booking.getNote());
        bookingResponseDTO.setStatus(booking.getStatus());
        bookingResponseDTO.setTotalAmount(booking.getTotalAmount());
        bookingResponseDTO.setNumberOfGuests(booking.getNumberOfGuests());
        bookingResponseDTO.setBookingDate(booking.getBookingTime());
        bookingResponseDTO.setUserId(booking.getUser().getId());
        bookingResponseDTO.setTableId(booking.getTable().getId());
        return bookingResponseDTO;
    }
}