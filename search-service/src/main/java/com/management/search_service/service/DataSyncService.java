package com.management.search_service.service;

import com.management.search_service.document.DishDocument;
import com.management.search_service.document.ReviewDocument;
import com.management.search_service.document.UserDocument;
import com.management.search_service.dto.DishSyncDto;
import com.management.search_service.dto.ReviewSyncDto;
import com.management.search_service.dto.UserSyncDto;
import com.management.search_service.repository.DishDocumentRepository;
import com.management.search_service.repository.ReviewDocumentRepository;
import com.management.search_service.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSyncService {
    private final DishDocumentRepository dishDocumentRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final RestTemplate restTemplate;

    @Value("${app.user-service.base-url:http://user-service:8081}")
    private String userServiceBaseUrl;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            log.info("Starting initial data synchronization...");
            try {
                syncAllData();
                log.info("Initial data synchronization completed successfully");
            } catch (Exception e) {
                log.error("Failed to sync initial data: {}", e.getMessage(), e);
            }
        };
    }

    public void syncAllData() {
        syncDishes();
        syncUsers();
        syncReviews();
    }

    private void syncDishes() {
        try {
            log.info("Syncing dishes from user-service...");
            String url = userServiceBaseUrl + "/api/dishes/all";

            ResponseEntity<List<DishSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<DishSyncDto>>() {}
            );

            List<DishSyncDto> dishes = response.getBody();
            if (dishes != null && !dishes.isEmpty()) {
                List<DishDocument> dishDocuments = dishes.stream()
                        .map(this::convertToDishDocument)
                        .collect(Collectors.toList());

                dishDocumentRepository.saveAll(dishDocuments);
                log.info("Synced {} dishes to Elasticsearch", dishDocuments.size());
            }
        } catch (Exception e) {
            log.error("Failed to sync dishes: {}", e.getMessage(), e);
        }
    }

    private void syncUsers() {
        try {
            log.info("Syncing users from user-service...");
            String url = userServiceBaseUrl + "/api/users/all";

            ResponseEntity<List<UserSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<UserSyncDto>>() {}
            );

            List<UserSyncDto> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                List<UserDocument> userDocuments = users.stream()
                        .map(this::convertToUserDocument)
                        .collect(Collectors.toList());

                userDocumentRepository.saveAll(userDocuments);
                log.info("Synced {} users to Elasticsearch", userDocuments.size());
            }
        } catch (Exception e) {
            log.error("Failed to sync users: {}", e.getMessage(), e);
        }
    }

    private void syncReviews() {
        try {
            log.info("Syncing reviews from user-service...");
            String url = userServiceBaseUrl + "/api/reviews/all";

            ResponseEntity<List<ReviewSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<ReviewSyncDto>>() {}
            );

            List<ReviewSyncDto> reviews = response.getBody();
            if (reviews != null && !reviews.isEmpty()) {
                List<ReviewDocument> reviewDocuments = reviews.stream()
                        .map(this::convertToReviewDocument)
                        .collect(Collectors.toList());

                reviewDocumentRepository.saveAll(reviewDocuments);
                log.info("Synced {} reviews to Elasticsearch", reviewDocuments.size());
            }
        } catch (Exception e) {
            log.error("Failed to sync reviews: {}", e.getMessage(), e);
        }
    }

    private DishDocument convertToDishDocument(DishSyncDto dto) {
        return DishDocument.builder()
                .id(dto.getId().toString())
                .dishId(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .isAvailable(dto.getIsAvailable())
                .category(dto.getCategory())
                .imageUrls(dto.getImageUrls())
                .averageRating(dto.getAverageRating())
                .totalReviews(dto.getTotalReviews())
                .orderCount(dto.getOrderCount())
                .createdAt(dto.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserDocument convertToUserDocument(UserSyncDto dto) {
        return UserDocument.builder()
                .id(dto.getId().toString())
                .userId(dto.getId())
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .roleName(dto.getRoleName())
                .status(dto.getStatus())
                .avatarUrl(dto.getAvatarUrl())
                .createdAt(dto.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ReviewDocument convertToReviewDocument(ReviewSyncDto dto) {
        return ReviewDocument.builder()
                .id(dto.getId().toString())
                .reviewId(dto.getId())
                .dishId(dto.getDishId())
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .customerAvatar(dto.getCustomerAvatar())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .isActive(dto.getIsActive())
                .isVerified(dto.getIsVerified())
                .createdAt(dto.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
