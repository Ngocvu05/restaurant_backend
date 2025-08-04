package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.service.AdminBookingService;
import com.management.restaurant.common.BookingStatus;
import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.BookingMapper;
import com.management.restaurant.model.Booking;
import com.management.restaurant.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBookingServiceImpl implements AdminBookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingDTO> getAlls() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO getById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()
                            -> new NotFoundException("Booking not found"));

        return bookingMapper.toDTO(booking);
    }

    @Override
    public BookingDTO create(BookingDTO dto) {
        Booking booking = bookingMapper.toEntity(dto);
        booking.setId(null);
        return bookingMapper.toDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingDTO update(Long id, BookingDTO dto) {
        if (dto.getId() == null) {
            throw new NotFoundException("Booking not found");
        }
        Booking existBooking = bookingRepository.findById(id).orElseThrow(()
                            -> new NotFoundException("Booking not found"));

        existBooking.setId(id);
        existBooking.setBookingTime(dto.getBookingTime());
        existBooking.setNote(dto.getNote());
        existBooking.setStatus(BookingStatus.valueOf(dto.getStatus()));
        existBooking.setNumberOfGuests(dto.getNumberOfGuests());

        return bookingMapper.toDTO(bookingRepository.save(existBooking));
    }

    @Override
    public void delete(long id) {
        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    @Override
    public BookingDTO cancelBooking(Long id) {
        Booking existBooking = bookingRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Booking not found"));
        existBooking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toDTO(bookingRepository.save(existBooking));
    }
}
