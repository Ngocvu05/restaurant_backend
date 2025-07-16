package com.management.restaurant.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadDTO {
    private MultipartFile file;

    private Long userId;
    private Long dishId;
}
