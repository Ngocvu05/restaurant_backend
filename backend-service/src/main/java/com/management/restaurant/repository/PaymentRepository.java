package com.management.restaurant.repository;

import com.management.restaurant.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByBooking_Id(Long bookingId);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    Optional<Payment> findFirstByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.status = 'PENDING'")
    List<Payment> findPendingPaymentsByBookingId(@Param("bookingId") Long bookingId);
}