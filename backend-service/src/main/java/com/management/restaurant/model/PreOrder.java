package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "preorders")
@EqualsAndHashCode(exclude = {"booking"})
@ToString(exclude = {"booking"})
public class PreOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("booking-preorders")
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"preOrders", "hibernateLazyInitializer", "handler"})
    private Dish dish;

    private int quantity;
    private String note;
}
