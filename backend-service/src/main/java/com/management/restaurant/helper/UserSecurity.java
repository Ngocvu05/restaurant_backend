package com.management.restaurant.helper;

import org.osgi.service.component.annotations.Component;
import org.springframework.security.core.Authentication;

@Component
public class UserSecurity {
    /**
     * Check if authenticated user is the owner
     */
    public boolean isOwner(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get current user ID from authentication
        // This assumes UserDetails contains user ID
        // Adjust based on your UserDetails implementation
        String username = authentication.getName();
        // You'll need to implement getUserIdFromUsername
        // or adjust this logic to match your auth setup

        return true; // Placeholder - implement actual logic
    }
}
