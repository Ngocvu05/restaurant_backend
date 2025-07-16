package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.dto.DashboardDTO;
import com.management.restaurant.admin.service.DashboardService;
import com.management.restaurant.common.BookingStatusCount;
import com.management.restaurant.model.Booking;
import com.management.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
        dto.setTotalRevenue(bookingRepository.sumTotalAmount()); // viết custom query
        return dto;
    }

    @Override
    public List<Booking> getRevenueByDate(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findRevenueBetweenDates(start, end);
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
