package com.management.restaurant.service.implement;

import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.DishMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.service.DishService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final ImageRepository imageRepository;

    @Override
    public List<DishDTO> getAllDishes() {
        return dishRepository.findAll().stream()
                .map(dishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DishDTO getDishById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found with id: " + id));
        return dishMapper.toDTO(dish);
    }

    @Override
    public DishDTO createDish(DishDTO dishDTO) {
        Dish dish = dishMapper.toEntity(dishDTO);

        if (dishDTO.getImageUrls() != null) {
            List<Image> images = dishDTO.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .url(String.valueOf(url))
                            .dish(dish)
                            .build())
                    .collect(Collectors.toList());
            dish.setImages(images);
        }
        List<Image> images = dishDTO.getImageUrls().stream()
                .map(url -> Image.builder().url(url).dish(dish).build())
                .collect(Collectors.toList());

        dish.setImages(images);
        Dish saved = dishRepository.save(dish);
        return dishMapper.toDTO(saved);
    }

    @Override
    public DishDTO updateDish(Long id, DishDTO dishDTO) {
        Dish existingDish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found"));

        // Cập nhật các trường cơ bản
        existingDish.setName(dishDTO.getName());
        existingDish.setDescription(dishDTO.getDescription());
        existingDish.setPrice(dishDTO.getPrice());
        existingDish.setIsAvailable(dishDTO.isAvailable());
        existingDish.setCategory(dishDTO.getCategory());

        // Cập nhật lại danh sách ảnh
        imageRepository.deleteAll(existingDish.getImages()); // xoá ảnh cũ

        if (dishDTO.getImageUrls() != null) {
            List<Image> updatedImages = dishDTO.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .dish(existingDish)
                            .build())
                    .collect(Collectors.toList());
            existingDish.setImages(updatedImages);
        }
        Dish saved = dishRepository.save(existingDish);
        return dishMapper.toDTO(saved);
    }

    @Override
    public void deleteDish(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found with id: " + id));
        dishRepository.delete(dish);
    }
}
