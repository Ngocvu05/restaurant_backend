package com.management.restaurant.service.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2ProviderFactory {
    private final Map<String, OAuth2Provider> providers;

    public OAuth2Provider getProvider(String providerName) {
        OAuth2Provider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerName);
        }
        return provider;
    }
}
