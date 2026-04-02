package com.management.restaurant.model;

import com.management.restaurant.contains.PaymentMethod;
import com.management.restaurant.contains.PaymentStatus;
import com.management.restaurant.model.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments", indexes = {
        @Index(name = "idx_booking_id", columnList = "booking_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_transaction_ref", columnList = "transaction_reference")
})
@EqualsAndHashCode(callSuper = true, exclude = {"booking"})
@ToString(exclude = {"booking"})
public class Payment extends SoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_time")
    @Builder.Default
    private LocalDateTime paymentTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_reference", length = 100, unique = true)
    private String transactionReference;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by", length = 100)
    private String processedBy;

    // Business logic
    public void markAsPaid(String processedBy) {
        this.status = PaymentStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public void refund(String processedBy) {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        this.status = PaymentStatus.REFUNDED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
    }

    public boolean isPaid() {
        return status == PaymentStatus.SUCCESS;
    }
}