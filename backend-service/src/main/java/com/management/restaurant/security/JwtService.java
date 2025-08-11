package com.management.restaurant.security;

import com.management.restaurant.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Service
@Slf4j
@RefreshScope
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

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

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        // ✅ Debug key creation
        Key key = getSignKey();

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info(">>> DEBUG - Generated token: {}", token);

        // ✅ Check token parse session
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
     * ✅ Extract username from token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * ✅ Validate token with user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token expiration check failed: {}", e.getMessage());
            return true;
        }
    }

    /**
     * ✅ Extract all claims (payload) from xa token
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT claims: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * ✅ Safe way to extract claims with error handling
     */
    public Claims getClaimsFromToken(String token) {
        return getClaims(token);
    }

    /**
     * ✅ Check if token is valid (syntax + signature + expiration)
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Build UserPrincipal from token claims
     */
    public UserPrincipal getUserFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        Long id = claims.get("id", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        return new UserPrincipal(
                id,
                username,
                null, // ❗️Never store password in token
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
