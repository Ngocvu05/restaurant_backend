package com.management.restaurant.common;

import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.UserRoleRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class UserRoleCache {
    private final Map<RoleName, UserRole> roleCache = new EnumMap<>(RoleName.class);

    public UserRoleCache(UserRoleRepository userRoleRepository) {
        List<UserRole> allRoles = userRoleRepository.findAll();
        for (UserRole role : allRoles) {
            roleCache.put(role.getName(), role);
        }
    }

    public UserRole getByRoleName(RoleName roleName) {
        return roleCache.get(roleName);
    }
}