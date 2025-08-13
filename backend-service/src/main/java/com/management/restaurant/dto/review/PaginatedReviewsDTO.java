package com.management.restaurant.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedReviewsDTO {
    private List<ReviewResponseDTO> reviews;
    private PaginationDTO pagination;
}