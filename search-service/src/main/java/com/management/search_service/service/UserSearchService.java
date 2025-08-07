package com.management.search_service.service;

import com.management.search_service.document.UserDocument;
import com.management.search_service.events.implement.UserEvent;
import com.management.search_service.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {
    private final UserDocumentRepository userDocumentRepository;

    public void indexUser(UserEvent event) {
        try {
            UserDocument document = UserDocument.builder()
                    .id(event.getUserId().toString())
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .fullName(event.getFullName())
                    .email(event.getEmail())
                    .phoneNumber(event.getPhoneNumber())
                    .address(event.getAddress())
                    .roleName(event.getRoleName())
                    .status(event.getStatus())
                    .avatarUrl(event.getAvatarUrl())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userDocumentRepository.save(document);
            log.info("Indexed user document: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to index user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteUser(Long userId) {
        try {
            userDocumentRepository.deleteByUserId(userId);
            log.info("Deleted user document: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<UserDocument> searchUsers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return userDocumentRepository.findAll(pageable);
        }
        return userDocumentRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                query, query, pageable);
    }

    public Page<UserDocument> findByRole(String roleName, Pageable pageable) {
        return userDocumentRepository.findByRoleName(roleName, pageable);
    }

    public Page<UserDocument> findByStatus(String status, Pageable pageable) {
        return userDocumentRepository.findByStatus(status, pageable);
    }

    public Optional<UserDocument> findById(Long userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    public Optional<UserDocument> findByUsername(String username) {
        return userDocumentRepository.findByUsername(username);
    }

    public Optional<UserDocument> findByEmail(String email) {
        return userDocumentRepository.findByEmail(email);
    }
}
