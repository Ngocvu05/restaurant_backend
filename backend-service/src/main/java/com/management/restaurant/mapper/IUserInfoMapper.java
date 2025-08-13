package com.management.restaurant.mapper;

import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.model.User;

public interface IUserInfoMapper {
    UserInfoDTO toDTO(User user);

    User toEntity(UserInfoDTO userDTO);
}