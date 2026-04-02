package com.management.restaurant.config;

import com.management.restaurant.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide current user for JPA Auditing
 * <p>
 * This class automatically populates @CreatedBy and @LastModifiedBy fields
 * by extracting username from Spring Security Context
 * <p>
 * Flow:
 * 1. User makes API request with JWT token
 * 2. JwtAuthFilter validates token and sets Authentication in SecurityContext
 * 3. When entity is saved, JPA Auditing calls getCurrentAuditor()
 * 4. This method extracts username from SecurityContext
 * 5. Username is saved to created_by/updated_by fields
 */
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {
    /**
     * Get current auditor (username) from Spring Security Context
     *
     * @return Optional containing username, or "system" if no authentication
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            // Get authentication from SecurityContext
            // This is set by JwtAuthFilter after validating JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Case 1: No authentication found
            // Happens during:
            // - System operations (scheduled jobs, background tasks)
            // - Database initialization
            // - Server startup
            if (authentication == null || !authentication.isAuthenticated()) {
                log.info("No authentication found, using 'system' as auditor");
                return Optional.of("system");
            }

            // Case 2: Anonymous user
            // Happens when:
            // - Accessing public endpoints
            // - No JWT token provided
            if ("anonymousUser".equals(authentication.getPrincipal())) {
                log.info("Anonymous user detected, using 'anonymous' as auditor");
                return Optional.of("automation");
            }

            // Case 3: UserPrincipal (MAIN CASE)
            // This is the normal flow when user is authenticated with JWT
            // JwtAuthFilter creates UserPrincipal from JWT token
            if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
                String username = userPrincipal.getUsername();
                log.info("Current auditor from UserPrincipal: {}", username);
                return Optional.of(username);
            }

            // Case 4: String principal (fallback)
            // Some authentication mechanisms return username directly as String
            if (authentication.getPrincipal() instanceof String principal) {
                log.info("String principal detected: {}", principal);
                return Optional.of(principal);
            }

            // Case 5: Unknown principal type (should rarely happen)
            log.warn("Unknown principal type: {}, using 'system' as fallback",
                    authentication.getPrincipal().getClass().getName());
            return Optional.of("system");

        } catch (Exception e) {
            // If anything goes wrong, don't fail the entire operation
            // Just log the error and return "system" as fallback
            log.error("Error getting current auditor: {}", e.getMessage(), e);
            return Optional.of("system");
        }
    }
}