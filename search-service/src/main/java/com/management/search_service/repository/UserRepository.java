package com.management.search_service.repository;

import com.management.search_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Search users by keyword (username, fullName, email)
     */
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND u.deletedAt IS NULL")
    Page<User> findByUsernameContainingOrFullNameContainingOrEmailContaining(
            @Param("keyword") String keyword1,
            @Param("keyword") String keyword2,
            @Param("keyword") String keyword3,
            Pageable pageable
    );
}
