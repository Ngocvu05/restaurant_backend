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
    private Long userId;  // nếu ảnh thuộc về người dùng
    private Long dishId;  // nếu ảnh thuộc về món ăn
}
