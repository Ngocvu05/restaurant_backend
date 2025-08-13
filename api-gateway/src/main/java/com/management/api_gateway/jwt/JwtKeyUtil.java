package com.management.api_gateway.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Slf4j
public class JwtKeyUtil {
    /**
     * Generate signing key with consistent Base64 detection logic
     * Used by both JwtService and JwtAuthenticationFilter
     */
    public static Key getSigningKey(String jwtSecret) {
        // Debug logging
        log.debug(">>> JWT Secret length: {}", jwtSecret.length());
        log.debug(">>> JWT Secret first 10 chars: {}",
                jwtSecret.substring(0, Math.min(jwtSecret.length(), 10)));

        // Check if secret is Base64 encoded
        try {
            byte[] decoded = Base64.getDecoder().decode(jwtSecret);
            log.debug(">>> Secret appears to be Base64 encoded");
            log.debug(">>> Decoded bytes length: {}", decoded.length);

            // If secret is Base64, decode before using
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            log.debug(">>> Secret is NOT Base64, using raw string");
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            log.debug(">>> Raw string bytes length: {}", keyBytes.length);

            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * Validate that the key generation is working correctly
     */
    public static void validateKeyGeneration(String jwtSecret) {
        try {
            Key key = getSigningKey(jwtSecret);
            log.info(">>> Key generation successful - Algorithm: {}, Format: {}",
                    key.getAlgorithm(), key.getFormat());
        } catch (Exception e) {
            log.error(">>> Key generation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT secret configuration", e);
        }
    }
}