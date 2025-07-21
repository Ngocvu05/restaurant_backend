package com.management.restaurant.service.implement;

import com.management.restaurant.common.RoleName;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.service.AuthService;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.ChatEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ChatEventProducer chatEventProducer;

    @Override
    public AuthResponse register(RegisterRequest request, MultipartFile avatarFile) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone_number(request.getPhone_number());
        user.setAddress(request.getAddress());
        user.setRole(userRoleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role not found")));
        userRepository.save(user);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String url = fileStorageService.save(avatarFile); // /uploads/uuid_filename.jpg

            Image avatar = new Image();
            avatar.setUrl(url);
            avatar.setAvatar(true); // ✅ cờ avatar
            avatar.setUser(user);
            imageRepository.save(avatar);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(null,token, user.getUsername(), user.getRole().getName().name(), getAvatarUrl(user), user.getEmail(), user.getFullName(), null);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
        return new AuthResponse(user.getId(),token, user.getUsername(), user.getRole().getName().name(), getAvatarUrl(user), user.getEmail(), user.getFullName(), null);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        User user = tokenEntity.getUser();
        String newAccessToken = jwtService.generateToken(user);
        return new AuthResponse(null,newAccessToken, user.getUsername(), user.getRole().getName().name(), getAvatarUrl(user), user.getEmail(), user.getFullName(), refreshToken);
    }


    private String getAvatarUrl(User user) {
        user.setImages(imageRepository.findImageByUser(user));
        return user.getImages().stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }

    private UserRole getRole(RoleName roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }
}
