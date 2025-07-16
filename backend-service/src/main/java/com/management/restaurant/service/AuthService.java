package com.management.restaurant.service;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(RegisterRequest request, MultipartFile file);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}
