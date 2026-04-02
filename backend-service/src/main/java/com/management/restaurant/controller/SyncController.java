package com.management.restaurant.controller;

import com.management.restaurant.dto.DishSyncDto;
import com.management.restaurant.dto.ReviewSyncDto;
import com.management.restaurant.dto.UserSyncDto;
import com.management.restaurant.service.SyncDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sync API for search-service to fetch data
 * Only accessible by ADMIN or SYSTEM users
 */
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {
    private final SyncDataService syncDataService;

    /**
     * Get all dishes for sync
     * Requires ADMIN or SYSTEM role
     */
    @GetMapping("/dishes/all")
    @PreAuthorize("hasAnyAuthority('ADMIN') or hasAnyRole('ADMIN')")
    public ResponseEntity<List<DishSyncDto>> getAllDishesForSync() {
        try {
            log.info("📥 Sync request received for all dishes");
            List<DishSyncDto> dishes = syncDataService.getAllDishesForSync();
            log.info("✅ Returning {} dishes for sync", dishes.size());
            return ResponseEntity.ok(dishes);
        } catch (Exception e) {
            log.error("❌ Error syncing dishes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync dishes: " + e.getMessage(), e);
        }
    }

    /**
     * Get all users for sync
     * Requires ADMIN or SYSTEM role
     */
    @GetMapping("/users/all")
    @PreAuthorize("hasAnyAuthority('ADMIN') or hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserSyncDto>> getAllUsersForSync() {
        try {
            log.info("📥 Sync request received for all users");
            List<UserSyncDto> users = syncDataService.getAllUsersForSync();
            log.info("✅ Returning {} users for sync", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error syncing users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync users: " + e.getMessage(), e);
        }
    }

    /**
     * Get all reviews for sync
     * Requires ADMIN or SYSTEM role
     */
    @GetMapping("/reviews/all")
    @PreAuthorize("hasAnyAuthority('ADMIN') or hasAnyRole('ADMIN')")
    public ResponseEntity<List<ReviewSyncDto>> getAllReviewsForSync() {
        try {
            log.info("📥 Sync request received for all reviews");
            List<ReviewSyncDto> reviews = syncDataService.getAllReviewsForSync();
            log.info("✅ Returning {} reviews for sync", reviews.size());
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("❌ Error syncing reviews: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync reviews: " + e.getMessage(), e);
        }
    }
}