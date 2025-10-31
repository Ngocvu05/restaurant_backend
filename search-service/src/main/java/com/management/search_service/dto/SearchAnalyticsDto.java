package com.management.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAnalyticsDto {
    private String keyword;
    private Long searchCount;
    private Long resultCount;
    private Double averageClickPosition;
    private LocalDateTime lastSearched;
}
