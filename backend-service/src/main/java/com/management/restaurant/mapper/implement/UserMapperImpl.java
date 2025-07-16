package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.mapper.UserMapper;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserDTO toDTO(User user) {
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

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .phone_number(user.getPhone_number())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .roleType(user.getRole().getName())
                .images(images)
                .build();
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(userDTO.getId());
        userDTO.setRoleType(userDTO.getRoleType());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setAddress(userDTO.getAddress());
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setPhone_number(userDTO.getPhone_number());

        // Convert ImageDTOs to Image entities
        if (userDTO.getImages() != null) {
            List<Image> images = userDTO.getImages().stream()
                    .map(imgDto -> {
                        Image img = new Image();
                        img.setId(imgDto.getId());
                        img.setUrl(imgDto.getUrl());
                        img.setAvatar(imgDto.isAvatar());
                        img.setUser(user);
                        return img;
                    }).toList();
            user.setImages(images);
        }
        return user;
    }
}
