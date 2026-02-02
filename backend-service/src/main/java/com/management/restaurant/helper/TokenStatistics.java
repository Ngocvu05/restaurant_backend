package com.management.restaurant.helper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenStatistics {
    private long totalTokens;
    private long activeTokens;
    private long expiredTokens;
    private long revokedTokens;
}
