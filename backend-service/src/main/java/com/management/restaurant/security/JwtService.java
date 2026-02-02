package com.management.restaurant.security;

import com.management.restaurant.dto.security.TokenInfo;
import com.management.restaurant.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Service with RS256 (Asymmetric Encryption)
 * <p>
 * Security Benefits of RS256 over HS256:
 * 1. Private key never leaves the server
 * 2. Public key can be shared for token verification
 * 3. Better for microservices architecture
 * 4. Prevents secret key leakage in client-side code
 * 5. Supports key rotation without service interruption
 */
@Service
@Slf4j
@RefreshScope
public class JwtService {
    @Value("${jwt.private-key-path:classpath:keys/private_key.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiration:900000}") // 15 minutes
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days
    private long refreshTokenExpiration;

    @Value("${jwt.issuer:restaurant-service}")
    private String issuer;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // ========================================
    // INITIALIZATION
    // ========================================

    @PostConstruct
    public void init() throws Exception {
        log.info("🔑 Initializing JWT Service with RS256...");
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
        log.info("✅ JWT Keys loaded successfully");
    }

    /**
     * Load Private Key from PEM file
     */
    private PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //String key = new String(Files.readAllBytes(privateKeyResource.getFile().toPath()))
        String key = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        log.info("🔐 Private key loaded from: {}", privateKeyResource.getFilename());
        return kf.generatePrivate(spec);
    }

    /**
     * Load Public Key from PEM file
     */
    private PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //String key = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()))
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

    // ========================================
    // TOKEN GENERATION
    // ========================================

    /**
     * Generate Access Token with RS256
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + accessTokenExpiration);

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", "ROLE_" + user.getRole().getName().name())
                .claim("type", "access")
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setNotBefore(now)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        log.info("✅ Generated access token for user: {} (expires in {} minutes)",
                user.getUsername(), accessTokenExpiration / 60000);

        return token;
    }

    /**
     * Generate Refresh Token with RS256
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)  // 🔑 RS256
                .compact();

        log.info("✅ Generated refresh token for user: {} (expires in {} days)",
                user.getUsername(), refreshTokenExpiration / 86400000);

        return token;
    }

    // ========================================
    // TOKEN VALIDATION
    // ========================================

    /**
     * Validate Access Token using Public Key
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Check 1: Issuer validation
            if (!issuer.equals(claims.getIssuer())) {
                log.warn("❌ Invalid issuer: {}", claims.getIssuer());
                return false;
            }

            // Check 2: Expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("❌ Token expired for user: {}", claims.getSubject());
                return false;
            }

            // Check 3: Not Before
            if (claims.getNotBefore() != null && claims.getNotBefore().after(new Date())) {
                log.warn("❌ Token not yet valid for user: {}", claims.getSubject());
                return false;
            }

            // Check 4: Token type (must be "access")
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("❌ Invalid token type: {} for user: {}", tokenType, claims.getSubject());
                return false;
            }

            // Check 5: Token age
            Date issuedAt = claims.getIssuedAt();
            if (issuedAt != null) {
                long tokenAge = System.currentTimeMillis() - issuedAt.getTime();
                if (tokenAge > accessTokenExpiration + 60000) {
                    log.warn("❌ Token too old for user: {} (age: {} ms)",
                            claims.getSubject(), tokenAge);
                    return false;
                }
            }

            log.debug("✅ Token validated successfully for user: {}", claims.getSubject());
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
     * Validate Refresh Token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Check issuer
            if (!issuer.equals(claims.getIssuer())) {
                log.warn("❌ Invalid refresh token issuer");
                return false;
            }

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

    // ========================================
    // TOKEN PARSING
    // ========================================

    /**
     * Get Claims using Public Key
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)  // 🔑 Use public key for verification
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
     * Get token ID (jti)
     */
    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    /**
     * Get token expiration
     */
    public Date getTokenExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Get remaining time in seconds
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
     * Build UserPrincipal from token
     */
    public UserPrincipal getUserFromToken(String token) {
        Claims claims = getClaims(token);

        Long id = claims.get("id", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        return new UserPrincipal(
                id,
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * Get detailed token information
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
                    .issuer(claims.getIssuer())
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

    /**
     * Get Public Key as String (for sharing with other services)
     */
    public String getPublicKeyString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}