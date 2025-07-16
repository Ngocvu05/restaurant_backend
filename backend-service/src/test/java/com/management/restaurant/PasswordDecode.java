package com.management.restaurant;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordDecode {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = "$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e";
        String decodedPassword = encoder.matches("admin123", encodedPassword) ? "admin123" : "Password does not match";
        System.out.println("Decoded Password: " + decodedPassword);
    }
}
