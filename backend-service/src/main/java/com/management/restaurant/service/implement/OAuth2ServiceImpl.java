package com.management.restaurant.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.management.restaurant.common.RoleName;
import com.management.restaurant.config.EnvConfig;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.ChatEventProducer;
import com.management.restaurant.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final ImageRepository imageRepository;
    private final ChatEventProducer chatEventProducer;
    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AuthResponse authenticateOAuth2User(OAuth2LoginRequest request) {
        try {
            log.info("Login attempt: request={}", request);

            // Find or create a user based on the provider
            User user;
            if ("google".equals(request.getProvider())) {
                user = processGoogleLogin(request);
            } else if ("facebook".equals(request.getProvider())) {
                user = processFacebookLogin(request);
            } else {
                throw new IllegalArgumentException("Provider not supported: " + request.getProvider());
            }

            // Generate JWT token for our system
            String token = jwtService.generateToken(user);

            // Send chat event if sessionId exists
            if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
                chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
            }

            return new AuthResponse(
                    user.getId(),
                    token,
                    user.getUsername(),
                    user.getRole().getName().name(),
                    getAvatarUrl(user),
                    user.getEmail(),
                    user.getFullName(),
                    null
            );
        } catch (Exception e) {
            log.error("OAuth2 authentication failed", e);
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage());
        }
    }

    /**
     * Processes Google login. Verifies the ID token and finds/creates a user.
     */
    private User processGoogleLogin(OAuth2LoginRequest request) throws Exception {
        // The token from frontend is an ID Token (JWT)
        String idTokenString = request.getAccessToken();

        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        return findOrCreateUser(email, name, pictureUrl);
    }

    /**
     * Verifies a Google ID Token using the Google API Client library.
     * This is the recommended modern approach.
     */
    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) throws Exception {
        String googleClientId = EnvConfig.get("GOOGLE_CLIENT_ID");
        log.info("GOOGLE_CLIENT_ID: " + googleClientId);
        if (googleClientId.isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID not found in environment variables or .env file");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid ID Token");
        }
        return idToken.getPayload();
    }

    /**
     * Processes Facebook login. Verifies the access token and finds/creates a user.
     */
    private User processFacebookLogin(OAuth2LoginRequest request) throws IOException {
        String accessToken = request.getAccessToken();
        String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;

        String response = restTemplate.getForObject(url, String.class);
        JsonNode jsonNode = objectMapper.readTree(response);

        if (!jsonNode.has("id") || !request.getProviderId().equals(jsonNode.get("id").asText())) {
            throw new IllegalArgumentException("Invalid Facebook Token");
        }

        String email = jsonNode.get("email").asText();
        String name = jsonNode.get("name").asText();
        String pictureUrl = jsonNode.path("picture").path("data").path("url").asText(null);

        return findOrCreateUser(email, name, pictureUrl);
    }

    /**
     * A generic method to find a user by email or create a new one if not found.
     * Updated to handle duplicate emails safely.
     */
    private User findOrCreateUser(String email, String fullName, String pictureUrl) {
        // Use findAll to handle multiple results safely
        List<User> users = userRepository.findAllByEmail(email);

        if (users.isEmpty()) {
            // Create new user if not found
            return createNewUser(email, fullName, pictureUrl);
        } else if (users.size() == 1) {
            // Single user found - update and return
            User user = users.getFirst();
            return updateExistingUser(user, fullName);
        } else {
            // Multiple users found - handle duplicate
            log.warn("Multiple users found with email: {}. Count: {}", email, users.size());

            // Strategy 1: Use the most recent user (by ID or creation date)
            User user = users.stream()
                    .max((u1, u2) -> u1.getId().compareTo(u2.getId()))
                    .orElse(users.getFirst());

            log.info("Selected user with ID: {} for email: {}", user.getId(), email);
            return updateExistingUser(user, fullName);

            // Alternative Strategy 2: Merge users (more complex)
            // return mergeUsers(users, fullName, pictureUrl);
        }
    }

    private User createNewUser(String email, String fullName, String pictureUrl) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setUsername(generateUniqueUsername(email));
        newUser.setPassword(""); // OAuth2 user has no local password
        newUser.setRole(userRoleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Default role 'CUSTOMER' not found")));

        User savedUser = userRepository.save(newUser);

        // Download and save avatar if available
        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            saveAvatarFromUrl(savedUser, pictureUrl);
        }

        log.info("Created new OAuth2 user: {}", savedUser.getUsername());
        return savedUser;
    }

    private User updateExistingUser(User user, String fullName) {
        // Update user's full name if it has changed
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

    private void saveAvatarFromUrl(User user, String imageUrl) {
        try {
            // Download image từ URL
            URL url = new URL(imageUrl);
            byte[] imageBytes = url.openStream().readAllBytes();

            // Upload image to Cloudinary
            String fileName = "avatar_" + user.getId() + "_" + UUID.randomUUID().toString();

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "restaurant/avatars", // Folder to save Avatar
                    "public_id", fileName,
                    "resource_type", "image"
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(imageBytes, uploadOptions);

            String cloudinaryUrl = (String) uploadResult.get("secure_url");

            // Save avatar to DB
            Image avatar = new Image();
            avatar.setUrl(cloudinaryUrl);
            avatar.setAvatar(true);
            avatar.setUser(user);
            imageRepository.save(avatar);

            log.info("Saved avatar to Cloudinary for user: {}", user.getUsername());
        } catch (IOException e) {
            log.error("Failed to save avatar from URL: {}", imageUrl, e);
        }
    }

    private String getAvatarUrl(User user) {
        // This logic can be simplified if the relationship is well-defined
        return imageRepository.findImageByUser(user).stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }
}