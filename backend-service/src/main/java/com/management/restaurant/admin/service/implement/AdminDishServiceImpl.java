package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.admin.mapper.DishAdminMapper;
import com.management.restaurant.admin.service.AdminDishService;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDishServiceImpl implements AdminDishService {
    private final DishRepository dishRepository;
    private final DishAdminMapper dishMapper;
    private final ImageRepository imageRepository;

    @Override
    public List<DishAdminDTO> getAll() {

        return dishRepository.findAll().stream()
                .map(dishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DishAdminDTO getById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found"));
        return dishMapper.toDTO(dish);
    }

    @Override
    public List<DishAdminDTO> getAvailableDishes() {
        return dishRepository.findByIsAvailable(true).stream()
                .map(dishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DishAdminDTO create(DishAdminDTO dto) {
        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setIsAvailable(dto.getIsAvailable());
        dish.setCategory(dto.getCategory());
        dish.setCreatedAt(LocalDateTime.now());
        dish.setIsAvailable(true);
        Dish savedDish = dishRepository.save(dish);

        associateImagesWithDish(dto.getImageUrls(), savedDish);

        return dishMapper.toDTO(savedDish);
    }

    @Override
    public DishAdminDTO update(Long id, DishAdminDTO dto) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found"));

        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setIsAvailable(dto.getIsAvailable());
        dish.setCategory(dto.getCategory());

        associateImagesWithDish(dto.getImageUrls(), dish);
        return dishMapper.toDTO(dishRepository.save(dish));
    }

    @Override
    public void delete(Long id) {
        dishRepository.deleteById(id);
    }

    private void associateImagesWithDish(List<String> imageUrls, Dish dish) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        List<Image> imagesToUpdate = imageUrls.stream()
                .map(imageRepository::findByUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(image -> image.setDish(dish))
                .toList();

        imageRepository.saveAll(imagesToUpdate);
    }
}