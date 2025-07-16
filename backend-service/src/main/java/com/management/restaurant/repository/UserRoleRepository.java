package com.management.restaurant.repository;

import com.management.restaurant.common.RoleName;
import com.management.restaurant.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByName(RoleName name);

}
