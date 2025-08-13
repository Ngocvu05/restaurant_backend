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
public class ImageDTO {
    private Long id;
    private String url;
    private LocalDateTime uploadedAt;
    private boolean isAvatar;
    private Long userId;
    private Long dishId;
}