package com.management.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl; // URL to the user's avatar image
    private String phone_number; // User's phone number
    private String role; // e.g., "USER", "ADMIN"
    //private boolean isActive; // Indicates if the user is active or not
}
