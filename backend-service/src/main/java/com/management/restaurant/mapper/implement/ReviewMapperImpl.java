package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.review.ReviewDTO;
import com.management.restaurant.mapper.ReviewMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Review;
import com.management.restaurant.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapperImpl implements ReviewMapper {
    @Autowired
    private DishRepository dishRepository;

    @Override
    public ReviewDTO toDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
                .id(review.getId())
                .dishId(review.getDishId())
                .customerName(review.getCustomerName())
                .customerEmail(review.getCustomerEmail())
                .customerAvatar(review.getCustomerAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .isActive(review.getIsActive())
                .isVerified(review.getIsVerified())
                .ipAddress(review.getIpAddress())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                // Dish information
                .dishName(review.getDish() != null ? review.getDish().getName() : null)
                .dishCategory(review.getDish() != null ? review.getDish().getCategory() : null)
                .build();
    }

    @Override
    public Review toEntity(ReviewDTO reviewDTO) {
        if (reviewDTO == null) return null;

        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setDishId(reviewDTO.getDishId());
        review.setCustomerName(reviewDTO.getCustomerName());
        review.setCustomerEmail(reviewDTO.getCustomerEmail());
        review.setCustomerAvatar(reviewDTO.getCustomerAvatar());
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        // Set default values for new entities
        if (reviewDTO.getId() == null) {
            review.setIsActive(true);
            review.setIsVerified(false);
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review.setIsActive(reviewDTO.getIsActive());
            review.setIsVerified(reviewDTO.getIsVerified());
            review.setCreatedAt(reviewDTO.getCreatedAt());
            review.setUpdatedAt(reviewDTO.getUpdatedAt());
        }

        review.setIpAddress(reviewDTO.getIpAddress());

        // Load dish relationship if dishId is provided
        if (reviewDTO.getDishId() != null) {
            Dish dish = dishRepository.findById(reviewDTO.getDishId()).orElse(null);
            review.setDish(dish);
        }

        return review;
    }

    @Override
    public List<ReviewDTO> toDTOList(List<Review> reviews) {
        if (reviews == null) return null;

        return reviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> toEntityList(List<ReviewDTO> reviewDTOs) {
        if (reviewDTOs == null) return null;

        return reviewDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDTO(ReviewDTO reviewDTO, Review review) {
        if (reviewDTO == null || review == null) return;

        // Update only allowed fields (ignore id, createdAt, dishId)
        if (reviewDTO.getCustomerName() != null) {
            review.setCustomerName(reviewDTO.getCustomerName());
        }
        if (reviewDTO.getCustomerEmail() != null) {
            review.setCustomerEmail(reviewDTO.getCustomerEmail());
        }
        if (reviewDTO.getCustomerAvatar() != null) {
            review.setCustomerAvatar(reviewDTO.getCustomerAvatar());
        }
        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment());
        }
        if (reviewDTO.getIsActive() != null) {
            review.setIsActive(reviewDTO.getIsActive());
        }
        if (reviewDTO.getIsVerified() != null) {
            review.setIsVerified(reviewDTO.getIsVerified());
        }

        // Always update the updatedAt timestamp
        review.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public ReviewDTO toSimpleDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
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
                .updatedAt(review.getUpdatedAt())
                // Exclude dish information
                .dishName(null)
                .dishCategory(null)
                .build();
    }

    @Override
    public ReviewDTO toPublicDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
                .id(review.getId())
                .dishId(review.getDishId())
                .customerName(review.getCustomerName())
                // Exclude sensitive information
                .customerEmail(null)
                .customerAvatar(review.getCustomerAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .isActive(review.getIsActive())
                .isVerified(review.getIsVerified())
                .ipAddress(null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                // Include dish information
                .dishName(review.getDish() != null ? review.getDish().getName() : null)
                .dishCategory(review.getDish() != null ? review.getDish().getCategory() : null)
                .build();
    }

    @Override
    public List<ReviewDTO> toSimpleDTOList(List<Review> reviews) {
        if (reviews == null) return null;

        return reviews.stream()
                .map(this::toSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> toPublicDTOList(List<Review> reviews) {
        if (reviews == null) return null;

        return reviews.stream()
                .map(this::toPublicDTO)
                .collect(Collectors.toList());
    }
}
