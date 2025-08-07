package com.management.search_service.controller;

import com.management.search_service.document.ReviewDocument;
import com.management.search_service.service.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/search/reviews")
@RequiredArgsConstructor
public class ReviewSearchController {
    private final ReviewSearchService reviewSearchService;

    @GetMapping
    public ResponseEntity<Page<ReviewDocument>> searchReviews(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReviewDocument> reviews = reviewSearchService.searchReviews(query, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/dish/{dishId}")
    public ResponseEntity<Page<ReviewDocument>> findByDish(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDocument> reviews = reviewSearchService.findByDish(dishId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/dish/{dishId}/active")
    public ResponseEntity<List<ReviewDocument>> findActiveReviewsByDish(@PathVariable Long dishId) {
        List<ReviewDocument> reviews = reviewSearchService.findActiveReviewsByDish(dishId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<Page<ReviewDocument>> findByRating(
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDocument> reviews = reviewSearchService.findByRating(rating, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ReviewDocument>> findActiveReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDocument> reviews = reviewSearchService.findActiveReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDocument> findById(@PathVariable Long id) {
        Optional<ReviewDocument> review = reviewSearchService.findById(id);
        return review.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}