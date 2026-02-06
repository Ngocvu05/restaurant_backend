package com.management.restaurant.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for OAuth2 Link information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LinkDTO {
    private Long id;
    private String provider;
    private String providerEmail;
    private String providerDisplayName;
    private String providerPictureUrl;
    private LocalDateTime linkedAt;
    private LocalDateTime lastUsedAt;
    private Boolean isPrimary;
}
