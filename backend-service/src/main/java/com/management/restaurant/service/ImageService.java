package com.management.restaurant.service;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.dto.ImageUploadDTO;
import com.management.restaurant.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    List<ImageDTO> getAllImages();

    ImageDTO getImageById(Long id);

    ImageDTO createImage(ImageDTO imageDTO);

    ImageDTO updateImage(Long id, ImageDTO imageDTO);

    void deleteImage(Long id);

    ImageDTO uploadImage(ImageUploadDTO uploadDTO);

    String saveImage(MultipartFile file) throws IOException;
}
