package com.management.restaurant.model;

import com.management.restaurant.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_history", indexes = {
        @Index(name = "idx_booking_id", columnList = "booking_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_dish_id", columnList = "dish_id")
})
@EqualsAndHashCode(callSuper = true, exclude = {"booking", "dish", "user"})
@ToString(exclude = {"booking", "dish", "user"})
public class OrderHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int quantity;

    @Builder.Default
    @Column(nullable = false)
    private Boolean served = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    public void markAsServed() {
        this.served = true;
    }

    public void calculateTotalAmount() {
        if (dish != null) {
            this.totalAmount = dish.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}