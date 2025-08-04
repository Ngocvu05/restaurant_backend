package com.management.restaurant.service.oauth2;

import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.model.User;

public interface OAuth2Provider {
    User authenticateAndGetUser(OAuth2LoginRequest request);
    String getProviderName();
}
