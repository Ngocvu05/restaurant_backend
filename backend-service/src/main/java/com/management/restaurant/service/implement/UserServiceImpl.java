package com.management.restaurant.service.implement;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.dto.UserInfoDTO;
import com.management.restaurant.event.EventPublisherService;
import com.management.restaurant.event.implement.UserEvent;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.IUserInfoMapper;
import com.management.restaurant.mapper.UserMapper;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
    private final IUserInfoMapper userInfoMapper;
    private final EventPublisherService eventPublisher;

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
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        UserRole role = userRoleRepository.findByName(userDTO.getRoleType())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRole(role);

        // Publish event
        UserEvent event = createUserEvent(user, UserEvent.Type.USER_CREATED);
        eventPublisher.publishUserEvent("user.created", event);

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
        // Publish event
        UserEvent event = createUserEvent(existing, UserEvent.Type.USER_UPDATED);
        eventPublisher.publishUserEvent("user.updated", event);

        return userMapper.toDTO(userRepository.save(existing));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
        // Publish event
        UserEvent event = createUserEvent(user, UserEvent.Type.USER_DELETED);
        eventPublisher.publishUserEvent("user.deleted", event);
    }

    @Override
    public void setAvatarImage(Long userId, Long imageId) throws BadRequestException {
        // Lấy ảnh
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        // Check if the image belongs to the user.
        if (!image.getUser().getId().equals(userId)) {
            throw new BadRequestException("Image does not belong to the user");
        }

        // Set the current image as the avatar.
        image.setAvatar(true);
        imageRepository.save(image);

        // Assign images that are not avatars.
        imageRepository.unsetAllOtherAvatars(userId, imageId);
    }

    /**
     * find Users by id.
     * @param userIds list ids of user.
     * @return list users' information.
     */
    @Override
    public List<UserInfoDTO> findUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds)
                .stream()
                .map(userInfoMapper::toDTO)
                .collect(Collectors.toList());
    }

    private UserEvent createUserEvent(User user, UserEvent.Type eventType) {
        return UserEvent.builder()
                .eventType(eventType.name())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone_number())
                .address(user.getAddress())
                .roleName(user.getRole() != null ? user.getRole().getName().name() : null)
                .status(user.getStatus().name())
                .avatarUrl(user.getImages() != null && !user.getImages().isEmpty() ?
                        user.getImages().stream()
                                .filter(img -> img.isAvatar())
                                .findFirst()
                                .map(img -> img.getUrl())
                                .orElse(null) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}