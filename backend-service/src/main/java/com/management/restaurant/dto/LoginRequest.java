package com.management.restaurant.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String sessionId;
}
