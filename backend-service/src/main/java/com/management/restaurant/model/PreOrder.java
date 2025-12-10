package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.management.restaurant.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Auditable;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "preorders", indexes = {
        @Index(name = "idx_booking_id", columnList = "booking_id"),
        @Index(name = "idx_dish_id", columnList = "dish_id")
})
@EqualsAndHashCode(callSuper = true, exclude = {"booking", "dish"})
@ToString(exclude = {"booking", "dish"})
public class PreOrder extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("booking-preorders")
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    @JsonIgnoreProperties({"preOrders", "hibernateLazyInitializer", "handler"})
    private Dish dish;

    @Column(nullable = false)
    private int quantity;

    @Column(columnDefinition = "TEXT")
    private String note;
}