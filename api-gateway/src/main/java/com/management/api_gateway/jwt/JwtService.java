package com.management.api_gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Service for API Gateway (RS256)
 * Only needs PUBLIC KEY for token verification
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.public-key-path:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiration:900000}") // 15 minutes
    private long accessTokenExpiration;

    @Value("${jwt.issuer:restaurant-service}")
    private String issuer;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        log.info("🔐 Initializing JWT Service for API Gateway (RS256)...");
        this.publicKey = loadPublicKey();
        log.info("✅ Public key loaded successfully");
    }

    /**
     * Load Public Key from PEM file
     */
    private PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        log.info("🔓 Public key loaded from: {}", publicKeyResource.getFilename());
        return kf.generatePublic(spec);
    }

    /**
     * Validate JWT Token using Public Key
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Check issuer
            if (!issuer.equals(claims.getIssuer())) {
                log.warn("❌ Invalid issuer: {}", claims.getIssuer());
                return false;
            }

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("❌ Token expired for user: {}", claims.getSubject());
                return false;
            }

            // Check not before
            if (claims.getNotBefore() != null && claims.getNotBefore().after(new Date())) {
                log.warn("❌ Token not yet valid");
                return false;
            }

            // Check token type
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("❌ Invalid token type: {}", tokenType);
                return false;
            }

            log.debug("✅ Token validated for user: {}", claims.getSubject());
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
     * Get Claims from token using Public Key
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)  // 🔑 Use public key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get User ID from token
     */
    public Long getUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    /**
     * Get user role from token
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
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
            return true;
        }
    }

    /**
     * Validate token age (prevent old tokens)
     */
    public boolean isTokenTooOld(String token, long maxAgeMinutes) {
        try {
            Date issuedAt = getClaims(token).getIssuedAt();
            if (issuedAt != null) {
                long ageMinutes = (System.currentTimeMillis() - issuedAt.getTime()) / 60000;
                return ageMinutes > maxAgeMinutes;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}