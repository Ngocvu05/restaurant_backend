package com.management.restaurant.service.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {
    public void logOAuth2Success(String provider, String email) {
        log.info("AUDIT: OAuth2 login SUCCESS - Provider: {}, Email: {}",
                provider, maskEmail(email));
    }

    public void logOAuth2Failure(String provider, String email, String error) {
        log.warn("AUDIT: OAuth2 login FAILURE - Provider: {}, Email: {}, Error: {}",
                provider, maskEmail(email), error);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "INVALID";
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username.charAt(0) + "*@" + domain;
        } else {
            return username.charAt(0) + "*".repeat(username.length() - 2) +
                    username.charAt(username.length() - 1) + "@" + domain;
        }
    }
}
