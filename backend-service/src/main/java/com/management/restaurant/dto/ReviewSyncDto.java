package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSyncDto {
    private Long id;
    private Long dishId;
    private String customerName;
    private String customerEmail;
    private String customerAvatar;
    private Integer rating;
    private String comment;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}