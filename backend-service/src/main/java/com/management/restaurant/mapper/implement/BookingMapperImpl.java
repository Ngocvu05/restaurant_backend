package com.management.restaurant.mapper.implement;

import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.mapper.BookingMapper;
import com.management.restaurant.model.*;
import com.management.restaurant.repository.DishRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapperImpl implements BookingMapper {
    @Autowired
    private DishRepository dishRepository;

    @Override
    public BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;

        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingTime(booking.getBookingTime());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setNumberOfPeople(booking.getNumberOfGuests()); // Same as numberOfGuests
        dto.setNote(booking.getNote());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        dto.setTotalAmount(booking.getTotalAmount());

        // ✅ Safely handle User
        if (booking.getUser() != null && Hibernate.isInitialized(booking.getUser())) {
            dto.setUserId(booking.getUser().getId());
            dto.setUsername(booking.getUser().getUsername());
        }

        // ✅ Safely handle Table
        if (booking.getTable() != null && Hibernate.isInitialized(booking.getTable())) {
            dto.setTableId(booking.getTable().getId());
        }

        // ✅ Safely handle PreOrders collection
        if (booking.getPreOrders() != null && Hibernate.isInitialized(booking.getPreOrders())) {
            dto.setPreOrderDishes(
                    booking.getPreOrders().stream()
                            .map(this::toPreOrderDTO)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setPreOrderDishes(Collections.emptyList());
        }

        return dto;
    }

    @Override
    public Booking toEntity(BookingDTO dto) {
        if (dto == null) return null;

        User user = null;
        if (dto.getUserId() != null) {
            user = User.builder()
                    .username(dto.getUsername())
                    .build();
            user.setId(dto.getUserId()); // Set ID via setter
        }

        // Build Table reference
        TableEntity table = null;
        if (dto.getTableId() != null) {
            table = TableEntity.builder().build();
            table.setId(dto.getTableId());
        }

        // Build main Booking entity
        Booking booking = Booking.builder()
                .user(user)
                .table(table)
                .bookingTime(dto.getBookingTime())
                .numberOfGuests(dto.getNumberOfGuests())
                .note(dto.getNote())
                .status(dto.getStatus() != null ? BookingStatus.valueOf(dto.getStatus()) : null)
                .totalAmount(dto.getTotalAmount())
                .build();

        // Set ID separately (not in builder for entities extending base)
        if (dto.getId() != null) {
            booking.setId(dto.getId());
        }

        // Build PreOrders
        if (dto.getPreOrderDishes() != null) {
            List<PreOrder> preorders = dto.getPreOrderDishes().stream()
                    .map(p -> {
                        Dish dish = dishRepository.findById(p.getDishId()).orElse(null);

                        PreOrder preorder = PreOrder.builder()
                                .dish(dish)
                                .booking(booking)
                                .quantity(p.getQuantity())
                                .note(p.getNote())
                                .build();

                        if (p.getId() != null) {
                            preorder.setId(p.getId());
                        }

                        return preorder;
                    })
                    .collect(Collectors.toList());
            booking.setPreOrders(preorders);
        }
        return booking;
    }

    private PreOrderDTO toPreOrderDTO(PreOrder preOrder) {
        if (preOrder == null) {
            return null;
        }

        PreOrderDTO dto = new PreOrderDTO();
        dto.setDishId(preOrder.getDish() != null ? preOrder.getDish().getId() : null);
        dto.setQuantity(preOrder.getQuantity());
        dto.setNote(preOrder.getNote());

        return dto;
    }
}