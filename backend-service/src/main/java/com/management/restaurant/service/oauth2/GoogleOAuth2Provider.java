package com.management.restaurant.service.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.management.restaurant.config.EnvConfig;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component("google")
@RequiredArgsConstructor
public class GoogleOAuth2Provider implements OAuth2Provider {
    private final UserOAuth2Service userService;

    @Override
    public User authenticateAndGetUser(OAuth2LoginRequest request) {
        try {
            GoogleIdToken.Payload payload = verifyGoogleIdToken(request.getAccessToken());

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            return userService.findOrCreateUser(email, name, pictureUrl);

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed", e);
        }
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) throws Exception {
        String googleClientId = EnvConfig.get("GOOGLE_CLIENT_ID");

        if (googleClientId == null || googleClientId.isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID not configured");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID Token");
        }

        return idToken.getPayload();
    }

    @Override
    public String getProviderName() {
        return "google";
    }
}
