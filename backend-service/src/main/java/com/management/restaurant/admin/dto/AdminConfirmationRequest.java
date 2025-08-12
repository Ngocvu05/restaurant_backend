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
    @NotBlank(message = "Ghi chú admin không được để trống")
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String adminNote;

    private String processedBy;
}
