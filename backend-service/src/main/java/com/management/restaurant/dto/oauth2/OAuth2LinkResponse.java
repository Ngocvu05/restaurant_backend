package com.management.restaurant.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OAuth2 link operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LinkResponse {
    private Boolean success;
    private String message;
    private OAuth2LinkDTO link;
}
