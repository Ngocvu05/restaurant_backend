package com.management.search_service.repository;

import com.management.search_service.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument,String> {
    Optional<UserDocument> findByUserId(Long userId);

    Page<UserDocument> findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String fullName, String username, Pageable pageable);

    Page<UserDocument> findByRoleName(String roleName, Pageable pageable);

    Page<UserDocument> findByStatus(String status, Pageable pageable);

    Optional<UserDocument> findByUsername(String username);

    Optional<UserDocument> findByEmail(String email);

    void deleteByUserId(Long userId);
}
