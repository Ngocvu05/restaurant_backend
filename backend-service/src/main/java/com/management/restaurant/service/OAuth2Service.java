package com.management.restaurant.service;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface OAuth2Service {
    AuthResponse authenticateOAuth2User(OAuth2LoginRequest request, HttpServletRequest httpRequest);
    AuthResponse refreshOAuth2Token(String refreshToken, HttpServletRequest httpRequest);
    void linkOAuth2Account(Long userId, OAuth2LoginRequest request);
    void unlinkOAuth2Account(Long userId, String provider);
}