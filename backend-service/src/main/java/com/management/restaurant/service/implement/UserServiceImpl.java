package com.management.restaurant.service.implement;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.UserMapper;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRoleRepository userRoleRepository;
    private final ImageRepository imageRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        UserRole role = userRoleRepository.findByName(userDTO.getRoleType())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRole(role);
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        existing.setUsername(existing.getUsername());
        existing.setFullName(userDTO.getFullName());
        existing.setEmail(userDTO.getEmail());
        existing.setPhone_number(userDTO.getPhone_number());
        existing.setAddress(userDTO.getAddress());
        return userMapper.toDTO(userRepository.save(existing));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void setAvatarImage(Long userId, Long imageId) throws BadRequestException {
        // Lấy ảnh
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        // Kiểm tra ảnh có thuộc user không
        if (!image.getUser().getId().equals(userId)) {
            throw new BadRequestException("Image does not belong to the user");
        }

        // Set ảnh hiện tại là avatar
        image.setAvatar(true);
        imageRepository.save(image);

        // Set các ảnh khác không phải avatar
        imageRepository.unsetAllOtherAvatars(userId, imageId);
    }

}
