package com.management.search_service.dto;

import com.management.search_service.document.DishDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSearchResultDto {
    private DishDocument dish;
    private Double score; // Relevance score
    private Map<String, List<String>> highlights; // Highlighted text
}
