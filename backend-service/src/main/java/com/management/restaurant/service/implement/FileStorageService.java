package com.management.restaurant.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final Cloudinary cloudinary;
    private final ImageService imageService;

    public String save(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "restaurant/upload/images",
                    "public_id", filename, // 👈 Tên file không chứa extension
                    "resource_type", "image" // đảm bảo là ảnh
            );

            // ✅ Fix: Sử dụng generic type chính xác
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);

            String imageUrl = (String) uploadResult.get("secure_url");

            ImageDTO imageDTO = new ImageDTO();
            imageDTO.setUrl(imageUrl);
            imageService.createImage(imageDTO);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public String saveAvatar(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "restaurant/avatars",
                    "public_id", filename, // file name, excludes extensions name
                    "resource_type", "image" // file is image
            );

            // ✅ Fix: Sử dụng generic type chính xác
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);

            String imageUrl = (String) uploadResult.get("secure_url");

            ImageDTO imageDTO = new ImageDTO();
            imageDTO.setUrl(imageUrl);
            imageService.createImage(imageDTO);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public void deleteByUrl(String url) {
        try {
            String publicId = extractPublicId(url);
            if (publicId != null) {
                // ✅ Fix: Sử dụng generic type chính xác
                @SuppressWarnings("unchecked")
                Map<String, Object> emptyMap = ObjectUtils.emptyMap();
                cloudinary.uploader().destroy(publicId, emptyMap);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    private String extractPublicId(String url) {
        try {
            URI uri = new URI(url);
            String[] parts = uri.getPath().split("/");
            // Ví dụ: /restaurant/upload/images/abc_xyz.jpg
            String folder = Arrays.stream(parts)
                    .skip(1) // bỏ qua phần rỗng đầu tiên ""
                    .limit(parts.length - 1)
                    .collect(Collectors.joining("/"));

            String filename = parts[parts.length - 1];
            String filenameWithoutExt = filename.contains(".") ?
                    filename.substring(0, filename.lastIndexOf(".")) : filename;

            return folder + "/" + filenameWithoutExt;
        } catch (Exception e) {
            return null;
        }
    }
}
