package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {
    private Long userId;
    private String token;
    private String username;
    private String role;
    private String avatarUrl;
    private String email;
    private String fullname;
    private String refreshToken;
}