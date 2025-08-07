package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.dto.DashboardDTO;
import com.management.restaurant.admin.dto.RevenueDTO;
import com.management.restaurant.admin.service.DashboardService;
import com.management.restaurant.common.BookingStatusCount;
import com.management.restaurant.model.Booking;
import com.management.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private final DishRepository dishRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public DashboardDTO getDashboardSummary() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalUsers(userRepository.count());
        dto.setTotalTables(tableRepository.count());
        dto.setTotalDishes(dishRepository.count());
        dto.setTotalBookings(bookingRepository.count());
        dto.setTotalRevenue(bookingRepository.sumTotalAmount());
        return dto;
    }

    @Override
    public List<Booking> getRevenueByDate(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findRevenueBetweenDates(start, end);

        // Group by date and sum revenue (optional - có thể làm trực tiếp trong SQL)
        Map<String, BigDecimal> revenueByDate = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBookingTime().toLocalDate().toString(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));

        // Log for debugging
        System.out.println("Revenue by date: " + revenueByDate);

        return bookings;
    }

    @Override
    public List<RevenueDTO> getRevenueGroupedByDate(LocalDateTime start, LocalDateTime end) {
        try {
            List<Booking> bookings = bookingRepository.findRevenueBetweenDates(start, end);

            if (bookings == null || bookings.isEmpty()) {
                log.info("No bookings found between {} and {}", start, end);
                return new ArrayList<>();
            }

            // Group bookings by date and sum revenue
            Map<String, BigDecimal> revenueByDate = bookings.stream()
                    .filter(booking -> booking.getBookingTime() != null && booking.getTotalAmount() != null)
                    .collect(Collectors.groupingBy(
                            booking -> booking.getBookingTime().toLocalDate().toString(),
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    booking -> booking.getTotalAmount(),
                                    BigDecimal::add
                            )
                    ));

            // Convert to DTO list and sort by date
            List<RevenueDTO> result = revenueByDate.entrySet().stream()
                    .map(entry -> new RevenueDTO(entry.getKey(), entry.getValue()))
                    .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                    .collect(Collectors.toList());

            log.info("Found {} revenue entries grouped by date", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error getting revenue grouped by date from {} to {}", start, end, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getBookingStatusStats() {
        List<BookingStatusCount> results = bookingRepository.countBookingsByStatus();
        Map<String, Long> stats = new HashMap<>();
        for (BookingStatusCount row : results) {
            stats.put(row.getStatus(), row.getCount());
        }
        return stats;
    }
}