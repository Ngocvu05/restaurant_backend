package com.management.restaurant.controller;

import com.management.restaurant.dto.DishSyncDto;
import com.management.restaurant.dto.ReviewSyncDto;
import com.management.restaurant.dto.UserSyncDto;
import com.management.restaurant.service.SyncDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {
    private final SyncDataService syncDataService;

    @GetMapping("/dishes/all")
    public ResponseEntity<List<DishSyncDto>> getAllDishesForSync() {
        List<DishSyncDto> dishes = syncDataService.getAllDishesForSync();
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<UserSyncDto>> getAllUsersForSync() {
        List<UserSyncDto> users = syncDataService.getAllUsersForSync();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/reviews/all")
    public ResponseEntity<List<ReviewSyncDto>> getAllReviewsForSync() {
        List<ReviewSyncDto> reviews = syncDataService.getAllReviewsForSync();
        return ResponseEntity.ok(reviews);
    }
}
