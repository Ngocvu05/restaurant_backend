package com.management.restaurant.service;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.dto.security.UserSecurityDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    Page<UserDTO> getAllUsers(Pageable pageable);

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getUsersByRole(String roleName);

    List<UserDTO> getActiveUsers();

    List<UserDTO> getUsersWithUnverifiedEmails();

    List<UserDTO> getLockedUsers();

    Page<UserDTO> searchUsers(String keyword, Pageable pageable);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    void clearAllUserCache();

    void warmUpCache();

    void unlockUserAccount(Long userId);

    void lockUserAccount(Long userId, int durationMinutes, String reason);

    void resetFailedLoginAttempts(Long userId);

    UserSecurityDTO getUserSecurityInfo(Long userId);

    void setAvatarImage(Long userId, Long imageId) throws BadRequestException;

    List<UserInfoDTO> findUsersByIds(List<Long> userIds);
}