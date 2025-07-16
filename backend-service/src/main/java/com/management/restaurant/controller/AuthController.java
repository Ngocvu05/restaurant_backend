package com.management.restaurant.controller;

import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.service.AuthService;
import com.management.restaurant.service.implement.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestPart("username") String username,
            @RequestPart("password") String password,
            @RequestPart("fullName") String fullName,
            @RequestPart("email") String email,
            @RequestPart("phone_number") String phone_number,
            @RequestPart("address") String address,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {
        log.info("📥 Đang xử lý đăng ký: {}", username);
        // 1. Lưu avatar nếu có
        String avatarUrl = null;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            avatarUrl = fileStorageService.save(avatarFile); // 🟡 bạn đã có service lưu file
        }

        // 2. Tạo request DTO
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .password(password)
                .fullName(fullName)
                .email(email)
                .phone_number(phone_number)
                .address(address)
                .avatarUrl(avatarUrl)
                .build();

        // 3. Gọi service đăng ký
        AuthResponse response = authService.register(request, avatarFile);
        // Send notification to admin
        notificationService.notifyAllAdmins("New Account", "Người dùng mới đã tạo tài khoản: " + request.getUsername());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }
}
