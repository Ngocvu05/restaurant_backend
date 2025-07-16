package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.mapper.PreOrderMapper;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.PreOrder;
import org.springframework.stereotype.Component;

@Component
public class PreOrderMapperImpl implements PreOrderMapper {
    @Override
    public PreOrderDTO toDTO(PreOrder preorder) {
        if (preorder == null) return null;

        return PreOrderDTO.builder()
                .id(preorder.getId())
                .bookingId(preorder.getBooking() != null ? preorder.getBooking().getId() : null)
                .dishId(preorder.getDish() != null ? preorder.getDish().getId() : null)
                .quantity(preorder.getQuantity())
                .note(preorder.getNote())
                .build();
    }

    @Override
    public PreOrder toEntity(PreOrderDTO dto) {
        if (dto == null) return null;

        PreOrder preorder = new PreOrder();
        preorder.setId(dto.getId());
        preorder.setQuantity(dto.getQuantity());
        preorder.setNote(dto.getNote());

        if (dto.getBookingId() != null) {
            Booking booking = new Booking();
            booking.setId(dto.getBookingId());
            preorder.setBooking(booking);
        }

        if (dto.getDishId() != null) {
            Dish dish = new Dish();
            dish.setId(dto.getDishId());
            preorder.setDish(dish);
        }

        return preorder;
    }
}
