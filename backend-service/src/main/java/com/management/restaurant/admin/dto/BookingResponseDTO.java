package com.management.restaurant.admin.dto;

import com.management.restaurant.common.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;
    private LocalDateTime bookingDate;
    private String note;
    private Integer numberOfGuests;
    private Integer numberOfPeople;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private Long tableId;
    private Long userId;
}
