package com.management.restaurant.service.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.restaurant.config.EnvConfig;
import com.management.restaurant.dto.FacebookTokenInfo;
import com.management.restaurant.dto.FacebookUserInfo;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component("facebook")
@RequiredArgsConstructor
public class FacebookOAuth2Provider implements  OAuth2Provider {
    private final UserOAuth2Service userService;
    private final ValidationService validationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Required Facebook permissions
    private static final List<String> REQUIRED_PERMISSIONS = Arrays.asList("email", "public_profile");

    // Facebook Graph API endpoints
    private static final String TOKEN_DEBUG_URL = "https://graph.facebook.com/debug_token";
    private static final String USER_INFO_URL = "https://graph.facebook.com/me";
    private static final String PERMISSIONS_URL = "https://graph.facebook.com/me/permissions";

    @Override
    public User authenticateAndGetUser(OAuth2LoginRequest request) {
        try {
            log.info("Starting Facebook authentication for user");

            validateRequest(request);
            FacebookTokenInfo tokenInfo = validateFacebookToken(request.getAccessToken());
            validatePermissions(request.getAccessToken());

            FacebookUserInfo userInfo = getUserInfo(request.getAccessToken());
            validateUserInfo(userInfo, request.getProviderId());

            log.info("Facebook authentication successful for user: {}", userInfo.getId());

            return userService.findOrCreateUser(
                    userInfo.getEmail(),
                    userInfo.getName(),
                    userInfo.getPictureUrl()
            );

        } catch (IllegalArgumentException e) {
            log.error("Facebook authentication validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Facebook authentication failed with unexpected error", e);
            throw new RuntimeException("Facebook authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate the OAuth2LoginRequest for Facebook-specific requirements
     */
    private void validateRequest(OAuth2LoginRequest request) {
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Facebook access token is required");
        }

        // Facebook might not always require providerId upfront, we'll get it from token validation
        log.debug("Facebook request validation passed");
    }

    /**
     * Validate Facebook access token using Facebook's debug_token endpoint
     * This is the most secure way to validate Facebook tokens
     */
    private FacebookTokenInfo validateFacebookToken(String accessToken) throws IOException {
        try {
            String appAccessToken = getAppAccessToken();
            String validationUrl = String.format(
                    "%s?input_token=%s&access_token=%s",
                    TOKEN_DEBUG_URL,
                    accessToken,
                    appAccessToken
            );

            log.debug("Validating Facebook token with Facebook API");
            String response = restTemplate.getForObject(validationUrl, String.class);
            JsonNode validationNode = objectMapper.readTree(response);

            if (validationNode.has("error")) {
                JsonNode error = validationNode.get("error");
                throw new IllegalArgumentException("Facebook token validation error: " + error.get("message").asText());
            }

            JsonNode data = validationNode.get("data");
            if (data == null) {
                throw new IllegalArgumentException("Invalid response from Facebook token validation");
            }

            return parseTokenInfo(data);

        } catch (RestClientException e) {
            log.error("Failed to connect to Facebook API for token validation", e);
            throw new RuntimeException("Failed to validate Facebook token - service unavailable", e);
        }
    }

    /**
     * Parse token information from Facebook's debug_token response
     */
    private FacebookTokenInfo parseTokenInfo(JsonNode data) {
        if (!data.get("is_valid").asBoolean()) {
            throw new IllegalArgumentException("Invalid Facebook access token");
        }

        String appId = data.get("app_id").asText();
        if (!EnvConfig.get("FACEBOOK_APP_ID").equals(appId)) {
            throw new IllegalArgumentException("Token was not issued for this application");
        }

        String userId = data.get("user_id").asText();
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Unable to get user ID from Facebook token");
        }

        // Check token expiry
        long expiresAt = data.has("expires_at") ? data.get("expires_at").asLong() : 0;
        if (expiresAt > 0 && System.currentTimeMillis() / 1000 > expiresAt) {
            throw new IllegalArgumentException("Facebook access token has expired");
        }

        // Check token type (should be USER for user tokens)
        String tokenType = data.has("type") ? data.get("type").asText() : "";
        if (!"USER".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid Facebook token type: " + tokenType);
        }

        log.debug("Facebook token validation successful for user: {}", userId);
        return new FacebookTokenInfo(userId, appId, expiresAt, true);
    }

    /**
     * Verify that the user has granted required permissions
     */
    private void validatePermissions(String accessToken) throws IOException {
        try {
            String permissionsUrl = String.format("%s?access_token=%s", PERMISSIONS_URL, accessToken);

            String response = restTemplate.getForObject(permissionsUrl, String.class);
            JsonNode permissionsNode = objectMapper.readTree(response);

            if (permissionsNode.has("error")) {
                log.warn("Unable to check Facebook permissions: {}", permissionsNode.get("error"));
                return; // Don't fail if we can't check permissions
            }

            JsonNode data = permissionsNode.get("data");
            if (data == null || !data.isArray()) {
                log.warn("Invalid permissions response from Facebook");
                return;
            }

            // Check for required permissions
            for (String requiredPermission : REQUIRED_PERMISSIONS) {
                boolean hasPermission = false;
                for (JsonNode permission : data) {
                    String permissionName = permission.get("permission").asText();
                    String status = permission.get("status").asText();

                    if (requiredPermission.equals(permissionName) && "granted".equals(status)) {
                        hasPermission = true;
                        break;
                    }
                }

                if (!hasPermission && "email".equals(requiredPermission)) {
                    throw new IllegalArgumentException("Email permission is required but not granted by user");
                }
            }

            log.debug("Facebook permissions validation completed");

        } catch (RestClientException e) {
            log.warn("Failed to validate Facebook permissions", e);
            // Don't fail the authentication for permission check failures
        }
    }

    /**
     * Get user information from Facebook Graph API
     */
    private FacebookUserInfo getUserInfo(String accessToken) throws IOException {
        try {
            String userInfoUrl = String.format(
                    "%s?fields=id,name,email,first_name,last_name,picture.width(200).height(200),verified&access_token=%s",
                    USER_INFO_URL,
                    accessToken
            );

            log.debug("Fetching user info from Facebook API");
            String response = restTemplate.getForObject(userInfoUrl, String.class);
            JsonNode userInfo = objectMapper.readTree(response);

            if (userInfo.has("error")) {
                JsonNode error = userInfo.get("error");
                String errorMessage = error.get("message").asText();
                String errorCode = error.has("code") ? error.get("code").asText() : "unknown";

                log.error("Facebook API error - Code: {}, Message: {}", errorCode, errorMessage);
                throw new IllegalArgumentException("Facebook API error: " + errorMessage);
            }

            return parseFacebookUserInfo(userInfo);

        } catch (RestClientException e) {
            log.error("Failed to connect to Facebook API for user info", e);
            throw new RuntimeException("Failed to get user information from Facebook", e);
        }
    }

    /**
     * Parse user information from Facebook Graph API response
     */
    private FacebookUserInfo parseFacebookUserInfo(JsonNode userInfo) {
        String id = userInfo.get("id").asText();
        String name = userInfo.has("name") ? userInfo.get("name").asText() : "";
        String email = userInfo.has("email") ? userInfo.get("email").asText() : null;
        String firstName = userInfo.has("first_name") ? userInfo.get("first_name").asText() : "";
        String lastName = userInfo.has("last_name") ? userInfo.get("last_name").asText() : "";
        boolean verified = userInfo.has("verified") && userInfo.get("verified").asBoolean();

        // Get picture URL
        String pictureUrl = null;
        if (userInfo.has("picture")) {
            JsonNode picture = userInfo.get("picture");
            if (picture.has("data") && picture.get("data").has("url")) {
                pictureUrl = picture.get("data").get("url").asText();
            }
        }

        // Use full name, or combine first and last name
        if (name.isEmpty() && (!firstName.isEmpty() || !lastName.isEmpty())) {
            name = (firstName + " " + lastName).trim();
        }

        return new FacebookUserInfo(id, name, email, firstName, lastName, pictureUrl, verified);
    }

    /**
     * Validate the user information received from Facebook
     */
    private void validateUserInfo(FacebookUserInfo userInfo, String expectedUserId) {
        // Validate user ID if provided in request
        if (expectedUserId != null && !expectedUserId.equals(userInfo.getId())) {
            throw new IllegalArgumentException("Facebook user ID mismatch");
        }

        // Validate email
        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required but not provided by Facebook. Please ensure you grant email permission.");
        }

        if (!validationService.isValidEmail(userInfo.getEmail())) {
            throw new IllegalArgumentException("Invalid email format provided by Facebook: " + userInfo.getEmail());
        }

        // Validate name
        if (userInfo.getName() == null || userInfo.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required but not provided by Facebook");
        }

        if (userInfo.getName().length() > 100) {
            throw new IllegalArgumentException("Name provided by Facebook is too long");
        }

        log.debug("Facebook user info validation successful for user: {}", userInfo.getId());
    }

    /**
     * Get app access token for Facebook API calls
     */
    private String getAppAccessToken() {
        String appId = EnvConfig.get("FACEBOOK_APP_ID");
        String appSecret = EnvConfig.get("FACEBOOK_APP_SECRET");
        log.info("Facebook app ID: {}, app secret: {}", appId, appSecret);

        if (appId == null || appId.isEmpty()) {
            throw new IllegalStateException("FACEBOOK_APP_ID not configured");
        }

        if (appSecret == null || appSecret.isEmpty()) {
            throw new IllegalStateException("FACEBOOK_APP_SECRET not configured");
        }

        return appId + "|" + appSecret;
    }

    @Override
    public String getProviderName() {
        return "facebook";
    }
}
