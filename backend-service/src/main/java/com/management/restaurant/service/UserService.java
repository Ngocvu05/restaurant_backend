package com.management.restaurant.service;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.dto.UserInfoDTO;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    void setAvatarImage(Long userId, Long imageId) throws BadRequestException;

    List<UserInfoDTO> findUsersByIds(List<Long> userIds);
}
