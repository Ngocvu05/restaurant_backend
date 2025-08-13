package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.NotificationDTO;
import com.management.restaurant.model.Notification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationsForUser(String username);

    List<NotificationDTO> getAllByUsername(String username);

    void sendNotificationToUser(Long userId, String content);

    void markAsRead(Long id);

    void createNotification(String username, String title, String content);

    void notifyAllAdmins(String title, String content);

    Page<Notification> getNotificationsByUser(Long userId, int page, int size);

    void markAllAsRead(Long userId);

    List<Notification> getTopNNotificationsByUser(Long userId, int limit);
}