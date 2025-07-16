package com.management.restaurant.service;

import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.BookingDetailResponseDTO;

import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);

    BookingDTO getBookingById(Long id);

    List<BookingDTO> getAllBookings();

    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    void deleteBooking(Long id);

    List<BookingDTO> getBookingHistory(Long userId);

    BookingDetailResponseDTO getBookingDetail(Long bookingId);
}
