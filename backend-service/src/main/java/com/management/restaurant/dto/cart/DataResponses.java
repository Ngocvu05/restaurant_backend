package com.management.restaurant.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponses<T> {
    // Getters
    private boolean success;
    private String message;
    private T data;
    private String timestamp;

    public DataResponses(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static <T> DataResponses<T> success(String message, T data) {
        return new DataResponses<>(true, message, data);
    }

    public static <T> DataResponses<T> error(String message) {
        return new DataResponses<>(false, message, null);
    }
}