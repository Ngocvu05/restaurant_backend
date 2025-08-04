package com.management.restaurant.service.oauth2;

import com.management.restaurant.common.RoleName;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.service.implement.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOAuth2Service {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ImageRepository imageRepository;
    private final ValidationService validationService;
    private final FileStorageService fileStorageService;

    public User findOrCreateUser(String email, String fullName, String pictureUrl) {
        List<User> users = userRepository.findAllByEmail(email);

        if (users.isEmpty()) {
            return createNewUser(email, fullName, pictureUrl);
        } else if (users.size() == 1) {
            return updateExistingUser(users.get(0), fullName);
        } else {
            log.warn("Multiple users found with email: {}. Count: {}", email, users.size());
            User user = users.stream()
                    .max((u1, u2) -> u1.getId().compareTo(u2.getId()))
                    .orElse(users.get(0));
            return updateExistingUser(user, fullName);
        }
    }

    private User createNewUser(String email, String fullName, String pictureUrl) {
        if (!validationService.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address");
        }

        fullName = validationService.sanitizeInput(fullName);
        if (fullName == null || fullName.length() > 100) {
            throw new IllegalArgumentException("Invalid full name");
        }

        User newUser = new User();
        newUser.setEmail(email.toLowerCase().trim());
        newUser.setFullName(fullName);
        newUser.setUsername(generateUniqueUsername(email));
        newUser.setPassword("");
        newUser.setRole(userRoleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Default role 'CUSTOMER' not found")));

        User savedUser = userRepository.save(newUser);

        if (validationService.isValidImageUrl(pictureUrl)) {
            try {
                fileStorageService.saveAvatarFromUrl(savedUser, pictureUrl);
            } catch (Exception e) {
                log.warn("Failed to save avatar for new user: {}", savedUser.getUsername(), e);
            }
        }

        log.info("Created new OAuth2 user: {} with email: {}", savedUser.getUsername(), savedUser.getEmail());
        return savedUser;
    }

    private User updateExistingUser(User user, String fullName) {
        if (fullName != null && !fullName.equals(user.getFullName())) {
            user.setFullName(fullName);
            userRepository.save(user);
        }
        return user;
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    public String getAvatarUrl(User user) {
        return imageRepository.findImageByUser(user).stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }
}
