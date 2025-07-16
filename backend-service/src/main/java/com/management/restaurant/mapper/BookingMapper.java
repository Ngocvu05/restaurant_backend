package com.management.restaurant.mapper;

import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.model.Booking;

public interface BookingMapper {
    BookingDTO toDTO(Booking booking);

    Booking toEntity(BookingDTO dto);
}
