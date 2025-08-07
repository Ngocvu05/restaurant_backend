package com.management.search_service.controller;

import com.management.search_service.document.UserDocument;
import com.management.search_service.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSearchController {
    private final UserSearchService userSearchService;

    @GetMapping
    public ResponseEntity<Page<UserDocument>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserDocument> users = userSearchService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<Page<UserDocument>> findByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserDocument> users = userSearchService.findByRole(roleName, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<UserDocument>> findByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserDocument> users = userSearchService.findByStatus(status, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDocument> findById(@PathVariable Long id) {
        Optional<UserDocument> user = userSearchService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDocument> findByUsername(@PathVariable String username) {
        Optional<UserDocument> user = userSearchService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDocument> findByEmail(@PathVariable String email) {
        Optional<UserDocument> user = userSearchService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}