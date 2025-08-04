package com.management.restaurant.admin.service;

import com.management.restaurant.dto.BookingDTO;

import java.util.List;

public interface AdminBookingService {
    List<BookingDTO> getAlls();

    BookingDTO getById(Long id);

    BookingDTO create(BookingDTO dto);

    BookingDTO update(Long id, BookingDTO dto);
    void delete(long id);

    BookingDTO cancelBooking(Long id);
}
