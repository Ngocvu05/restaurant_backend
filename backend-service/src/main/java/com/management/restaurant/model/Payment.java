package com.management.restaurant.model;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.common.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Builder.Default
    private LocalDateTime paymentTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();

    @Column(name = "processed_by")
    private String processedBy;     // Admin username who processed this
}