package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.service.implement.FileStorageService;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/uploads")
@RequiredArgsConstructor
public class AdminUploadController {
    private final FileStorageService fileStorageService;
    private final ImageRepository imageRepository;
    private final NotificationService notificationService;

    @PostMapping("/images-dish")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.save(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            notificationService.notifyAllAdmins("Thêm ảnh món ăn mới", "Admin đã thêm một ảnh món ăn mới.");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/images-dish")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        try {
            Optional<Image> imageOpt = imageRepository.findByUrl(url);
            if (imageOpt.isPresent()) {
                Image image = imageOpt.get();
                // Optional: Xóa file khỏi hệ thống nếu bạn lưu nội bộ
                fileStorageService.deleteByUrl(url);
                // Delete record on database
                imageRepository.delete(image);
                return ResponseEntity.ok("Xoá ảnh thành công");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy ảnh");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xoá ảnh thất bại");
        }
    }
}