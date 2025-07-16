package com.management.restaurant.controller;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.dto.ImageUploadDTO;
import com.management.restaurant.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {
    private final ImageService imageService;

    @PostMapping("/upload-image")
    public ResponseEntity<ImageDTO> uploadImage(@ModelAttribute ImageUploadDTO imageUploadDTO) {
        ImageDTO savedImage = imageService.uploadImage(imageUploadDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }
}
