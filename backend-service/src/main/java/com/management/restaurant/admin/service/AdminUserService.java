package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.dto.UserDTO;

import java.util.List;

public interface AdminUserService {
    List<UserDTO> getAllUsers();

    UserDTO getById(Long id);

    UserDTO create(UserDTO dto);

    UserDTO update(Long id, UserDTO dto);

    void delete(Long id);
}