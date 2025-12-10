package com.management.restaurant.service;

import com.management.restaurant.dto.DishSyncDto;
import com.management.restaurant.dto.ReviewSyncDto;
import com.management.restaurant.dto.UserSyncDto;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Review;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ReviewRepository;
import com.management.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncDataService {
    private final DishRepository dishRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<DishSyncDto> getAllDishesForSync() {
        List<Dish> dishes = dishRepository.findAllWithImages();
        return dishes.stream()
                .map(this::convertToDishSyncDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSyncDto> getAllUsersForSync() {
        List<User> users = userRepository.findAllWithImages();
        return users.stream()
                .map(this::convertToUserSyncDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewSyncDto> getAllReviewsForSync() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToReviewSyncDto)
                .collect(Collectors.toList());
    }

    private DishSyncDto convertToDishSyncDto(Dish dish) {
        // Safely get images with null check
        List<String> imageUrls = null;
        try {
            if (dish.getImages() != null && !dish.getImages().isEmpty()) {
                imageUrls = dish.getImages().stream()
                        .map(img -> img.getUrl())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Could not load images for dish {}: {}", dish.getId(), e.getMessage());
            imageUrls = Collections.emptyList();  // ✅ Return empty list instead of null
        }
        return DishSyncDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .isAvailable(dish.getIsAvailable())
                .category(dish.getCategory())
                .imageUrls(imageUrls)
                .averageRating(dish.getAverageRating())
                .totalReviews(dish.getTotalReviews())
                .orderCount(dish.getOrderCount())
                .createdAt(dish.getCreatedAt())
                .build();
    }

    private UserSyncDto convertToUserSyncDto(User user) {
        String avatarUrl = null;
        try {
            if (user.getImages() != null && !user.getImages().isEmpty()) {
                avatarUrl = user.getImages().stream()
                        .filter(img -> img.isAvatar())
                        .findFirst()
                        .map(img -> img.getUrl())
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not load avatar for user {}: {}", user.getId(), e.getMessage());
        }

        return UserSyncDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone_number())
                .address(user.getAddress())
                .roleName(user.getRole() != null ? user.getRole().getName().name() : null)
                .status(user.getStatus().name())
                .avatarUrl(avatarUrl)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private ReviewSyncDto convertToReviewSyncDto(Review review) {
        return ReviewSyncDto.builder()
                .id(review.getId())
                .dishId(review.getDishId())
                .customerName(review.getCustomerName())
                .customerEmail(review.getCustomerEmail())
                .customerAvatar(review.getCustomerAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .isActive(review.getIsActive())
                .isVerified(review.getIsVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }
}