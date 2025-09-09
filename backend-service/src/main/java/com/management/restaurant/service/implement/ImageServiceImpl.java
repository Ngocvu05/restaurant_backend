package com.management.restaurant.service.implement;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.dto.ImageUploadDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.ImageMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    @Autowired
    private ImageMapper imageMapper;

    //@Value("${upload.dir}")
    private String uploadDir;

    @Override
    public List<ImageDTO> getAllImages() {
        return imageRepository.findAll().stream()
                .map(imageMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ImageDTO getImageById(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        return imageMapper.toDTO(image);
    }

    @Override
    public ImageDTO createImage(ImageDTO dto) {
        Image image = imageMapper.toEntity(dto);

        if (dto.getUserId() != null) {
            image.setUser(userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found")));
        }

        if (dto.getDishId() != null) {
            image.setDish(dishRepository.findById(dto.getDishId())
                    .orElseThrow(() -> new NotFoundException("Dish not found")));
        }

        image.setUploadedAt(LocalDateTime.now());
        return imageMapper.toDTO(imageRepository.save(image));
    }

    @Override
    public ImageDTO updateImage(Long id, ImageDTO dto) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        image.setUrl(dto.getUrl());

        if (dto.getUserId() != null) {
            image.setUser(userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found")));
        } else {
            image.setUser(null);
        }

        if (dto.getDishId() != null) {
            image.setDish(dishRepository.findById(dto.getDishId())
                    .orElseThrow(() -> new NotFoundException("Dish not found")));
        } else {
            image.setDish(null);
        }

        return imageMapper.toDTO(imageRepository.save(image));
    }

    @Override
    public void deleteImage(Long id) {
        if (!imageRepository.existsById(id)) {
            throw new NotFoundException("Image not found");
        }
        imageRepository.deleteById(id);
    }

    @Override
    public ImageDTO uploadImage(ImageUploadDTO dto) {
        MultipartFile file = dto.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }
        try {
            // Generate a unique file name.
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            // Absolute path to the images folder within the resources directory.
            String uploadDir = System.getProperty("user.dir") + "/uploads/images";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Store the file in the directory.
            Path filePath = Paths.get(uploadDir, fileName);
            file.transferTo(filePath.toFile());

            // Create an Image entity object.
            Image image = new Image();
            image.setUrl("uploads/images/" + fileName); // dùng đường dẫn public
            image.setUploadedAt(LocalDateTime.now());

            // Assign user if available.
            if (dto.getUserId() != null) {
                User user = userRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new NotFoundException("User not found with ID: " + dto.getUserId()));
                image.setUser(user);
            }

            // Assign dish if available
            if (dto.getDishId() != null) {
                Dish dish = dishRepository.findById(dto.getDishId())
                        .orElseThrow(() -> new NotFoundException("Dish not found with ID: " + dto.getDishId()));
                image.setDish(dish);
            }

            imageRepository.save(image);
            return imageMapper.toDTO(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public String saveImage(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/images";
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";

        String filename = UUID.randomUUID() + ext;
        Path filepath = Paths.get(uploadDir + filename);

        Files.createDirectories(filepath.getParent());
        Files.write(filepath, file.getBytes());

        return "/images/" + filename; // relative URL
    }
}