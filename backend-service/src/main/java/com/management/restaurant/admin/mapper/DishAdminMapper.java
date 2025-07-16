package com.management.restaurant.admin.mapper;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.model.Dish;

public interface DishAdminMapper {
    DishAdminDTO toDTO(Dish dish);
}
