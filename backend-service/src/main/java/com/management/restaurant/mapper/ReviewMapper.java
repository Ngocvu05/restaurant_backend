package com.management.restaurant.mapper;

import com.management.restaurant.dto.review.ReviewDTO;
import com.management.restaurant.model.Review;
import org.mapstruct.*;

import java.util.List;

public interface ReviewMapper {
    // Entity to DTO - Main mapping
    ReviewDTO toDTO(Review review);

    // DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isVerified", constant = "false")
    Review toEntity(ReviewDTO reviewDTO);

    // List mappings - specify which method to use for collection mapping
    @Named("toDTOList")
    default List<ReviewDTO> toDTOList(List<Review> reviews) {
        return reviews.stream().map(this::toDTO).toList();
    }

    List<Review> toEntityList(List<ReviewDTO> reviewDTOs);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dishId", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ReviewDTO reviewDTO, @MappingTarget Review review);

    // Simple DTO without dish information
    @Mapping(target = "dishName", ignore = true)
    @Mapping(target = "dishCategory", ignore = true)
    @Named("toSimpleDTO")
    ReviewDTO toSimpleDTO(Review review);

    // Public DTO without sensitive information
    @Mapping(target = "customerEmail", ignore = true)
    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "dishName", source = "dish.name")
    @Mapping(target = "dishCategory", source = "dish.category")
    @Named("toPublicDTO")
    ReviewDTO toPublicDTO(Review review);

    // Additional list mappings for specific DTOs
    @Named("toSimpleDTOList")
    default List<ReviewDTO> toSimpleDTOList(List<Review> reviews) {
        return reviews.stream().map(this::toSimpleDTO).toList();
    }

    @Named("toPublicDTOList")
    default List<ReviewDTO> toPublicDTOList(List<Review> reviews) {
        return reviews.stream().map(this::toPublicDTO).toList();
    }
}