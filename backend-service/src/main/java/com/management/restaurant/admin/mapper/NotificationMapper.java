package com.management.restaurant.admin.mapper;

import com.management.restaurant.admin.dto.NotificationDTO;
import com.management.restaurant.model.Notification;

public interface NotificationMapper {
    NotificationDTO toDTO(Notification notification);

    Notification toEntity(NotificationDTO dto);
}
