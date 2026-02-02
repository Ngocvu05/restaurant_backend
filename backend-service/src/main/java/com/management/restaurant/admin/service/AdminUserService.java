package com.management.restaurant.admin.service;

import com.management.restaurant.dto.UserDTO;

import java.util.List;

public interface AdminUserService {
    List<UserDTO> getAllUsers();

    UserDTO getById(Long id);

    UserDTO create(UserDTO dto);

    UserDTO update(Long id, UserDTO dto);

    void delete(Long id);

    /**
     * Admin: Lock user account
     */
    void lockUserAccount(Long userId, int durationMinutes);

    /**
     * Admin: Unlock user account
     */
    void unlockUserAccount(Long userId);
}