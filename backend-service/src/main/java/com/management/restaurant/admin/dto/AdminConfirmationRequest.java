package com.management.restaurant.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConfirmationRequest {
    @NotBlank(message = "Admin note must not be empty.")
    @Size(max = 500, message = "The note must not exceed 500 characters.")
    private String adminNote;

    private String processedBy;
}