package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request DTO for email verification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    private String token;
}
