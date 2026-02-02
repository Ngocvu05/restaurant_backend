package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request DTO for account locking (admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockAccountRequest {
    private Long userId;
    private Integer durationMinutes;  // 0 = indefinite
    private String reason;
}
