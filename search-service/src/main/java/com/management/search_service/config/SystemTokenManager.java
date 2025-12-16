package com.management.search_service.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemTokenManager {
    private final RestTemplate restTemplate;
    private String systemToken;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${system.user.username}")
    private String systemUsername;

    @Value("${system.user.password}")
    private String systemPassword;

    @Value("${system.token.retry.max-attempts}")
    private int maxRetryAttempts;

    @Value("${system.token.retry.delay-seconds}")
    private int retryDelaySeconds;

    @PostConstruct
    public void init() {
        log.info("🔐 Initializing System Token Manager...");
        log.info("📍 User Service URL: {}", userServiceUrl);
        log.info("👤 System Username: {}", systemUsername);
        generateSystemToken();
    }

    private void generateSystemToken() {
        int attempt = 0;

        while (attempt < maxRetryAttempts) {
            try {
                attempt++;
                log.info("🔄 Attempting to generate system token (attempt {}/{})", attempt, maxRetryAttempts);

                String loginUrl = userServiceUrl + "/api/v1/auth/login";
                log.info("📡 Calling: {}", loginUrl);

                Map<String, String> loginRequest = new HashMap<>();
                loginRequest.put("username", systemUsername);
                loginRequest.put("password", systemPassword);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    log.info("📥 Response body: {}", body);
                    String token = null;
                    if (body.containsKey("success") && body.get("success") == Boolean.TRUE) {
                        // Format has wrapper
                        Map<String, Object> data = (Map<String, Object>) body.get("data");
                        token = data.containsKey("accessToken")
                                ? (String) data.get("accessToken")
                                : (String) data.get("token");
                    } else if (body.containsKey("token")) {
                        token = (String) body.get("token");
                    }

                    if (token != null && !token.isEmpty()) {
                        this.systemToken = token;
                        log.info("✅ System JWT token generated successfully");
                        log.info("Token: {}...{}",
                                systemToken.substring(0, Math.min(20, systemToken.length())),
                                systemToken.substring(Math.max(0, systemToken.length() - 10)));
                        return;
                    }
                }

                throw new RuntimeException("Invalid response from auth service: " + response.getBody());

            } catch (Exception e) {
                log.error("❌ Failed to generate system JWT token (attempt {}/{}): {}",
                        attempt, maxRetryAttempts, e.getMessage());

                if (e.getMessage().contains("Connection refused")) {
                    log.warn("⚠️ User service not available yet. Will retry...");
                }

                if (attempt >= maxRetryAttempts) {
                    log.error("🚨 All retry attempts exhausted. System token generation failed!");
                    log.error("💡 Please check:");
                    log.error("   1. User service is running: {}", userServiceUrl);
                    log.error("   2. System user exists: {}", systemUsername);
                    log.error("   3. Network connectivity between services");
                    throw new RuntimeException("Failed to generate system token after " + maxRetryAttempts + " attempts", e);
                }

                try {
                    log.info("⏳ Waiting {} seconds before retry...", retryDelaySeconds);
                    TimeUnit.SECONDS.sleep(retryDelaySeconds);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Token generation interrupted", ie);
                }
            }
        }
    }

    public String getSystemToken() {
        if (systemToken == null || systemToken.isEmpty()) {
            log.warn("⚠️ System token is null or empty, attempting to regenerate...");
            generateSystemToken();
        }
        return systemToken;
    }

    public void refreshToken() {
        log.info("🔄 Manually refreshing system token...");
        generateSystemToken();
    }
}