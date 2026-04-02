package com.management.restaurant.security;

import com.management.restaurant.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RefreshScope
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Access token: 15 minutes (recommended for high security)
    // Can be adjusted based on your security requirements
    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private Key getSignKey() {
        // Checking type of secret key is Base64 or not
        try {
            byte[] decoded = Base64.getDecoder().decode(jwtSecret);
            // If secret is Base64, decoded before use
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            log.info(">>> DEBUG - Secret is NOT Base64, using raw string");
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Generate access token (short-lived)
     * <p>
     * Security features:
     * - jti (JWT ID) for token tracking and revocation
     * - iat (issued at) for age validation
     * - exp (expiration) - 15 minutes default
     * - nbf (not before) to prevent premature use
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + accessTokenExpiration);

        Key key = getSignKey();

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName().name())
                .claim("type", "access")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setNotBefore(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("Generated access token for user: {} (expires in {} minutes)",
                user.getUsername(), accessTokenExpiration / 60000);

        //  Check token parse session
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info(">>> DEBUG - Token validation success: {}", claims.getSubject());
        } catch (Exception e) {
            log.error(">>> DEBUG - Token validation failed: {}", e.getMessage());
        }

        return token;
    }

    /**
     * Generate REFRESH token (long-lived: 7 days)
     * <p>
     * Should be stored in database for:
     * - Token rotation
     * - Revocation capability
     * - Usage tracking
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("✅ Generated refresh token for user: {} (expires in {} days)",
                user.getUsername(), refreshTokenExpiration / 86400000);

        return token;
    }

    /**
     * Comprehensive token validation
     * <p>
     * Checks:
     * 1. Signature validity
     * 2. Token not expired
     * 3. Token is access type (not refresh)
     * 4. NotBefore timestamp valid
     * 5. Token age reasonable
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Check 1: Expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("❌ Token expired for user: {}", claims.getSubject());
                return false;
            }

            // Check 2: Not Before
            if (claims.getNotBefore() != null && claims.getNotBefore().after(new Date())) {
                log.warn("❌ Token not yet valid for user: {}", claims.getSubject());
                return false;
            }

            // Check 3: Token type (must be "access")
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("❌ Invalid token type: {} for user: {}", tokenType, claims.getSubject());
                return false;
            }

            // Check 4: Token age (prevent very old tokens)
            Date issuedAt = claims.getIssuedAt();
            if (issuedAt != null) {
                long tokenAge = System.currentTimeMillis() - issuedAt.getTime();
                if (tokenAge > accessTokenExpiration + 60000) { // +1 min grace period
                    log.warn("❌ Token too old for user: {} (age: {} ms)",
                            claims.getSubject(), tokenAge);
                    return false;
                }
            }

            log.info("✅ Token validated successfully for user: {}", claims.getSubject());
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("❌ Token expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Check token type
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                log.warn("❌ Invalid refresh token type: {}", tokenType);
                return false;
            }

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("❌ Refresh token expired");
                return false;
            }

            log.info("✅ Refresh token validated successfully");
            return true;
        } catch (Exception e) {
            log.error("❌ Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     *  Extract username from token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get token ID (jti) for revocation tracking
     */
    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    /**
     * Get token expiration date
     */
    public Date getTokenExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Get remaining time until token expires (in seconds)
     * Useful for Redis TTL when blacklisting
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("Failed to get token remaining time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get all claims from token
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Build UserPrincipal from token claims
     */
    public UserPrincipal getUserFromToken(String token) {
        Claims claims = getClaims(token);

        Long id = claims.get("id", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        return new UserPrincipal(
                id,
                username,
                null, // Never store password in token
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * Get detailed token information (for debugging)
     */
    public TokenInfo getTokenInfo(String token) {
        try {
            Claims claims = getClaims(token);

            return TokenInfo.builder()
                    .tokenId(claims.getId())
                    .username(claims.getSubject())
                    .userId(claims.get("id", Long.class))
                    .role(claims.get("role", String.class))
                    .type(claims.get("type", String.class))
                    .issuedAt(claims.getIssuedAt())
                    .expiresAt(claims.getExpiration())
                    .notBefore(claims.getNotBefore())
                    .isExpired(isTokenExpired(token))
                    .remainingSeconds(getTokenRemainingTime(token))
                    .build();
        } catch (Exception e) {
            log.error("Failed to get token info: {}", e.getMessage());
            return null;
        }
    }

    // ========================================
    // INNER CLASS: Token Information
    // ========================================

    @lombok.Data
    @lombok.Builder
    public static class TokenInfo {
        private String tokenId;
        private String username;
        private Long userId;
        private String role;
        private String type;
        private Date issuedAt;
        private Date expiresAt;
        private Date notBefore;
        private boolean isExpired;
        private long remainingSeconds;
    }
}