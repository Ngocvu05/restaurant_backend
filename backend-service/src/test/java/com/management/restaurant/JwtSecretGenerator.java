package com.management.restaurant;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        byte[] secret = new byte[32]; // 256-bit
        new SecureRandom().nextBytes(secret);
        String base64Secret = Base64.getEncoder().encodeToString(secret);
        System.out.println("Your JWT secret: " + base64Secret);
    }
}
