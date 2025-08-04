package com.management.restaurant.service;

import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;

public interface OAuth2Service {
    AuthResponse authenticateOAuth2User(OAuth2LoginRequest request);


}
