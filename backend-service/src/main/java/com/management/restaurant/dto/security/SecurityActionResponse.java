package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Response DTO for security actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityActionResponse {
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;
}
