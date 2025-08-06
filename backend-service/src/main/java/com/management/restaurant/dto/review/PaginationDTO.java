package com.management.restaurant.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationDTO {
    private Integer currentPage;
    private Integer totalPages;
    private Long totalItems;
    private Integer itemsPerPage;
    private Boolean hasNext;
    private Boolean hasPrev;
}
