package com.management.restaurant.repository;

import com.management.restaurant.common.RoleName;
import com.management.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAllByRole_Name(RoleName ADMIN);

    @Query("SELECT u FROM User u JOIN u.images i WHERE i.id = :imageId")
    Optional<User> findUserByImageId(@Param("imageId") Long imageId);
}
