package com.management.restaurant.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for linking OAuth2 account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkOAuth2Request {
    private String provider;
    private String accessToken;
    private String idToken;  // For providers like Google
}
