package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.mapper.IUserInfoMapper;
import com.management.restaurant.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserInfoMapperImpl implements IUserInfoMapper {
    @Override
    public UserInfoDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        List<ImageDTO> images = user.getImages() != null
                ? user.getImages().stream().map(image -> ImageDTO.builder()
                        .id(image.getId())
                        .url(image.getUrl())
                        .isAvatar(image.isAvatar())
                        .build())
                .toList()
                : new ArrayList<>();

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone_number(user.getPhone_number())
                .avatarUrl(
                        images.stream()
                                .filter(ImageDTO::isAvatar)
                                .map(ImageDTO::getUrl)
                                .findFirst()
                                .orElse(null))
                .build();
    }

    @Override
    public User toEntity(UserInfoDTO userDTO) {
        if (userDTO == null) {
            return  null;
        }
        return User.builder()
                .build();
    }
}
