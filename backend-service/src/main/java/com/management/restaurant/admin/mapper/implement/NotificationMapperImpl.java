package com.management.restaurant.admin.mapper.implement;

import com.management.restaurant.admin.dto.NotificationDTO;
import com.management.restaurant.admin.mapper.NotificationMapper;
import com.management.restaurant.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationMapperImpl implements NotificationMapper {
    @Override
    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }
        return Notification.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .isRead(dto.getIsRead())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
