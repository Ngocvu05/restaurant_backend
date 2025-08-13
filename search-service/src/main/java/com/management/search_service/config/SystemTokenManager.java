package com.management.search_service.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemTokenManager {

    private final RestTemplate restTemplate;
    private String jwt;

    public String getToken() {
        return jwt;
    }

    @PostConstruct
    public void init() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("username", "sync_data");
            body.put("password", "admin123");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            String userServiceUrl = "http://user-service:8081/api/v1/auth/login";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    userServiceUrl,
                    request,
                    Map.class
            );

            jwt = "Bearer " + response.getBody().get("token");
            log.info("✅ Search-service - Generated system JWT token {}: ", jwt);

        } catch (Exception e) {
            log.error("❌ Failed to generate system JWT token", e);
        }
    }
}