package com.management.search_service.service;

import com.management.search_service.config.SystemTokenManager;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
    private final SystemTokenManager systemTokenManager;

    @Value("${app.user-service.base-url:http://user-service:8081/api/v1/sync}")
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
            String url = userServiceBaseUrl + "/dishes/all";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<DishSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
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
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Authentication failed for dishes sync. Token might be invalid or expired: {}", e.getMessage());
            handleAuthError("dishes", e);
        } catch (Exception e) {
            log.error("Failed to sync dishes: {}", e.getMessage(), e);
        }
    }

    private void syncUsers() {
        try {
            log.info("Syncing users from user-service...");
            String url = userServiceBaseUrl + "/users/all";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<UserSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
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
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Authentication failed for users sync. Token might be invalid or expired: {}", e.getMessage());
            handleAuthError("users", e);
        } catch (Exception e) {
            log.error("Failed to sync users: {}", e.getMessage(), e);
        }
    }

    private void syncReviews() {
        try {
            log.info("Syncing reviews from user-service...");
            String url = userServiceBaseUrl + "/reviews/all";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ReviewSyncDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
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
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Authentication failed for reviews sync. Token might be invalid or expired: {}", e.getMessage());
            handleAuthError("reviews", e);
        } catch (Exception e) {
            log.error("Failed to sync reviews: {}", e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String token = systemTokenManager.getToken();

        // Log token info for debugging (without exposing actual token)
        log.debug("Using token for authentication - Token length: {}, Starts with 'Bearer': {}",
                token != null ? token.length() : 0,
                token != null && token.startsWith("Bearer"));

        // Ensure Bearer prefix is present
        if (token != null && !token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        return headers;
    }

    private void handleAuthError(String dataType, HttpClientErrorException.Unauthorized e) {
        // Log detailed error for debugging
        log.error("Authorization failed for {} sync:", dataType);
        log.error("  - Response status: {}", e.getStatusCode());
        log.error("  - Response headers: {}", e.getResponseHeaders());
        log.error("  - Token manager class: {}", systemTokenManager.getClass().getSimpleName());

        // Check if token is null or empty
        String token = systemTokenManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            log.error("  - Token is null or empty!");
        } else {
            log.debug("  - Token format appears valid (length: {})", token.length());
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