package com.management.restaurant.service.oauth2;

import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class ValidationService {
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex) && email.length() <= 254;
    }

    public boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        try {
            new URL(url);
            return url.startsWith("https://") &&
                    (url.contains("facebook.com") ||
                            url.contains("googleapis.com") ||
                            url.contains("googleusercontent.com"));
        } catch (Exception e) {
            return false;
        }
    }

    public String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
}
