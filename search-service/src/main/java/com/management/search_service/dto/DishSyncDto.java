package com.management.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishSyncDto {
    private Long id;
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