package com.management.restaurant.mapper.implement;

import com.management.restaurant.common.BookingStatus;
import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.mapper.BookingMapper;
import com.management.restaurant.model.*;
import com.management.restaurant.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapperImpl implements BookingMapper {
    @Autowired
    private DishRepository dishRepository;

    @Override
    public BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;

        return BookingDTO.builder()
                .id(booking.getId())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .username(booking.getUser() != null ? booking.getUser().getUsername() : null)
                .tableId(booking.getTable() != null ? booking.getTable().getId() : null)
                .bookingTime(booking.getBookingTime())
                .numberOfGuests(booking.getNumberOfGuests())
                .note(booking.getNote())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .preOrderDishes(booking.getPreOrders() != null
                        ? booking.getPreOrders().stream()
                        .map(pre -> PreOrderDTO.builder()
                                .id(pre.getId())
                                .bookingId(pre.getBooking() != null ? pre.getBooking().getId() : null)
                                .dishId(pre.getDish().getId())
                                .quantity(pre.getQuantity())
                                .note(pre.getNote())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .totalAmount(booking.getTotalAmount())
                .build();
    }

    @Override
    public Booking toEntity(BookingDTO dto) {
        if (dto == null) return null;

        Booking booking = new Booking();
        booking.setId(dto.getId());

        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            booking.setUser(user);
        } else if (dto.getUsername() != null) {
            User user = new User();
            user.setUsername(dto.getUsername());
            booking.setUser(user);
        }

        if (dto.getTableId() != null) {
            TableEntity table = new TableEntity();
            table.setId(dto.getTableId());
            booking.setTable(table);
        }

        booking.setBookingTime(dto.getBookingTime());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setNote(dto.getNote());

        if (dto.getStatus() != null) {
            booking.setStatus(BookingStatus.valueOf(dto.getStatus()));
        }

        if (dto.getPreOrderDishes() != null) {
            List<PreOrder> preorders = dto.getPreOrderDishes().stream()
                    .map(p -> {
                        PreOrder preorder = new PreOrder();
                        preorder.setQuantity(p.getQuantity());
                        preorder.setNote(p.getNote());
                        Dish dish = dishRepository.findById(p.getDishId()).orElse(null);
                        preorder.setDish(dish);
                        preorder.setBooking(booking);
                        return preorder;
                    })
                    .collect(Collectors.toList());
            booking.setPreOrders(preorders);
        }
        booking.setTotalAmount(dto.getTotalAmount());

        return booking;
    }
}