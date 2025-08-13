package com.management.restaurant.service.implement;

import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.event.EventPublisherService;
import com.management.restaurant.event.implement.DishEvent;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.DishMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.service.DishService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final ImageRepository imageRepository;
    private final EventPublisherService eventPublisher;

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

        // Publish event
        DishEvent event = createDishEvent(saved, DishEvent.Type.DISH_CREATED);
        eventPublisher.publishDishEvent("dish.created", event);

        return dishMapper.toDTO(saved);
    }

    @Override
    public DishDTO updateDish(Long id, DishDTO dishDTO) {
        Dish existingDish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found"));

        // update data
        existingDish.setName(dishDTO.getName());
        existingDish.setDescription(dishDTO.getDescription());
        existingDish.setPrice(dishDTO.getPrice());
        existingDish.setIsAvailable(dishDTO.isAvailable());
        existingDish.setCategory(dishDTO.getCategory());

        // update image list
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

        // Publish event
        DishEvent event = createDishEvent(saved, DishEvent.Type.DISH_UPDATED);
        eventPublisher.publishDishEvent("dish.updated", event);

        return dishMapper.toDTO(saved);
    }

    @Override
    public void deleteDish(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found with id: " + id));
        dishRepository.delete(dish);

        // Publish event
        DishEvent event = createDishEvent(dish, DishEvent.Type.DISH_DELETED);
        eventPublisher.publishDishEvent("dish.deleted", event);
    }

    private DishEvent createDishEvent(Dish dish, DishEvent.Type eventType) {
        return DishEvent.builder()
                .eventType(eventType.name())
                .dishId(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .isAvailable(dish.getIsAvailable())
                .category(dish.getCategory())
                .imageUrls(dish.getImages() != null ?
                        dish.getImages().stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList()) : null)
                .averageRating(dish.getAverageRating())
                .totalReviews(dish.getTotalReviews())
                .orderCount(dish.getOrderCount())
                .createdAt(dish.getCreatedAt())
                .build();
    }
}