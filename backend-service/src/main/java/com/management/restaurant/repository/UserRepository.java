package com.management.restaurant.repository;

import com.management.restaurant.contains.RoleName;
import com.management.restaurant.contains.UserStatus;
import com.management.restaurant.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByRole_Name(RoleName role_name);

    Optional<User> findByStatusAndDeletedAtIsNull(UserStatus status);

    Optional<User> findByEmailVerifiedFalse();

    Optional<User> findByAccountLockedUntilIsNotNull();

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByRole_Name(RoleName ADMIN);

    @Query("SELECT u FROM User u JOIN u.images i WHERE i.id = :imageId")
    Optional<User> findUserByImageId(@Param("imageId") Long imageId);

    List<User> findAllByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.images " +
            "LEFT JOIN FETCH u.role")
    List<User> findAllWithImages();

    boolean existsByEmail(String email);

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