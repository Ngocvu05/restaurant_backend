package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.management.restaurant.common.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

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
@EqualsAndHashCode(exclude = {"preOrders", "user", "table"})
@ToString(exclude = {"preOrders"})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false)
    @JsonIgnoreProperties({"bookings", "hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"bookings", "hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "table_id", nullable = false, insertable = false)
    private TableEntity table;

    private LocalDateTime bookingTime;
    private int numberOfGuests;
    private String note;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("booking-preorders")
    private List<PreOrder> preOrders = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<OrderHistory> orderHistories;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}