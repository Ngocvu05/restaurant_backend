package com.management.restaurant.admin.repository;

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
    /**
     * Tìm theo booking ID và trạng thái
     */
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, String status);

    /**
     * Kiểm tra xem có yêu cầu nào đang pending cho booking này không
     */
    boolean existsByBookingIdAndStatus(Long bookingId, String status);

    /**
     * Lấy tất cả theo trạng thái, sắp xếp theo thời gian tạo mới nhất
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * Lấy tất cả yêu cầu của một booking
     */
    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Lấy danh sách cần xử lý (PENDING) với thông tin booking
     */
    @Query("SELECT pc FROM Payment pc " +
            "LEFT JOIN FETCH pc.booking b " +
            "WHERE pc.status = :status " +
            "ORDER BY pc.createdAt DESC")
    List<Payment> findPendingWithBookingDetails(@Param("status") String status);

    /**
     * Thống kê theo trạng thái
     */
    @Query("SELECT pc.status, COUNT(pc) FROM Payment pc GROUP BY pc.status")
    List<Object[]> countByStatus();

    /**
     * Tìm theo khoảng thời gian
     */
    @Query("SELECT pc FROM Payment pc " +
            "WHERE pc.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY pc.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}