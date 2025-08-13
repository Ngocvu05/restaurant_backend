package com.management.search_service.events.implement;

import com.management.search_service.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DishEvent extends BaseEvent {
    public enum Type {
        DISH_CREATED,
        DISH_UPDATED,
        DISH_DELETED,
        DISH_AVAILABILITY_CHANGED,
        DISH_RATING_UPDATED
    }

    private Long dishId;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private String category;
    private List<String> imageUrls;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer orderCount;
    private LocalDateTime createdAt;
}