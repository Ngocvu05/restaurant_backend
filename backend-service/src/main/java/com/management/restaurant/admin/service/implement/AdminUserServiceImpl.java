package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.service.AdminUserService;
import com.management.restaurant.common.UserRoleCache;
import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.UserMapper;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository userRepository;
    private final UserRoleCache userRoleCache;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper :: toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO create(UserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(dto.getPassword())
                .phone_number(dto.getPhone_number())
                .build();

        if (dto.getRoleType() != null) {
            user.setRole(userRoleCache.getByRoleName(dto.getRoleType()));
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"));
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(dto.getPassword());
        user.setPhone_number(dto.getPhone_number());
        user.setAddress(dto.getAddress());
        user.setRole(userRoleCache.getByRoleName(dto.getRoleType()));
        user.setStatus(dto.getStatus());
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
