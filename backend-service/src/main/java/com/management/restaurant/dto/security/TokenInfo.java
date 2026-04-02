package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private String tokenId;
    private String username;
    private Long userId;
    private String role;
    private String type;
    private String issuer;
    private Date issuedAt;
    private Date expiresAt;
    private Date notBefore;
    private boolean isExpired;
    private long remainingSeconds;
}