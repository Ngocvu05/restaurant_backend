package com.management.restaurant.model;

import com.management.restaurant.common.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    private LocalDateTime bookingTime;
    private int numberOfGuests;
    private String note;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<PreOrder> preOrders = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<OrderHistory> orderHistories;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}