package com.management.restaurant.repository;

import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentConfirmationRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking_IdAndStatus(Long bookingId, PaymentStatus status);

    boolean existsByBookingIdAndStatus(Long bookingId, String status);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    @Query("SELECT pc FROM Payment pc " +
            "LEFT JOIN FETCH pc.booking b " +
            "WHERE pc.status = :status " +
            "ORDER BY pc.createdAt DESC")
    List<Payment> findPendingWithBookingDetails(@Param("status") String status);

    @Query("SELECT pc.status, COUNT(pc) FROM Payment pc GROUP BY pc.status")
    List<Object[]> countByStatus();

    @Query("SELECT pc FROM Payment pc " +
            "WHERE pc.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY pc.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    List<Payment> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);
}