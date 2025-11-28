package com.management.restaurant.analytics.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "cart_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSnapshot {
    @Id
    private String id;

    @Indexed
    private Long userId;

    private List<CartItemSnapshot> items;

    @Indexed
    private LocalDateTime snapshotTime;

    private String reason; // AUTO_SAVE, BEFORE_CHECKOUT, BEFORE_CLEAR
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CartItemSnapshot {
    private Long dishId;
    private String dishName;
    private Integer quantity;
    private java.math.BigDecimal price;
}
