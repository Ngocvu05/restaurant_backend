package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.DishAdminDTO;

import java.util.List;

public interface AdminDishService {
    List<DishAdminDTO> getAll();

    DishAdminDTO getById(Long id);

    DishAdminDTO create(DishAdminDTO dto);

    DishAdminDTO update(Long id, DishAdminDTO dto);

    void delete(Long id);

    List<DishAdminDTO> getAvailableDishes();
}
