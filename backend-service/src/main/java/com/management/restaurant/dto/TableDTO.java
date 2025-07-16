package com.management.restaurant.dto;

import com.management.restaurant.common.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDTO {
    private Long id;
    private String tableName;
    private int capacity;
    private String status;
    private String description;
}
