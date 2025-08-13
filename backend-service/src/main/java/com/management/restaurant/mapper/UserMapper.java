package com.management.restaurant.mapper;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.model.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapping;

public interface UserMapper {
    @Mapping(source = "role.name", target = "roleType")
    UserDTO toDTO(User user);

    @InheritInverseConfiguration
    User toEntity(UserDTO userDTO);
}