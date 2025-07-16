package com.management.restaurant.repository;

import com.management.restaurant.common.BookingStatusCount;
import com.management.restaurant.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b")
    BigDecimal sumTotalAmount();

    @Query("SELECT b.status AS status, COUNT(b) AS count FROM Booking b GROUP BY b.status")
    List<BookingStatusCount> countBookingsByStatus();

    @Query(value = "SELECT * FROM bookings WHERE booking_time BETWEEN :start AND :end", nativeQuery = true)
    List<Booking> findRevenueBetweenDates(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

}
