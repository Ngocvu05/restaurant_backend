package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.dto.NotificationDTO;
import com.management.restaurant.admin.mapper.NotificationMapper;
import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.common.RoleName;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.model.Notification;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.NotificationRepository;
import com.management.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<NotificationDTO> getAllByUsername(String username) {
        List<Notification> notifications = notificationRepository.findByToUserUsernameOrderByCreatedAtDesc(username);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsForUser(String username) {
        return notificationRepository.findByToUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }


    public void sendNotificationToUser(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setContent(content);
        notification.setToUser(user);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setTitle("Notification for " + user.getUsername());

        // Lưu notification vào DB
        notificationRepository.save(notification);
        NotificationDTO dto = notificationMapper.toDTO(notification);

        messagingTemplate.convertAndSendToUser(
                user.getUsername(), "/topic/notifications", dto
        );
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void createNotification(String username, String title, String message) {
        log.info("📥 [createNotification] Creating notification for user: {}", username);

        // B1: Tìm user từ DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        log.info("👤 Found user ID: {}", user.getId());

        // B2: Tạo và lưu notification
        Notification notification = Notification.builder()
                .toUser(user)
                .title(title)
                .content(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("💾 Saved notification: {}", notification.getContent());

        // B3: Convert sang DTO để gửi WebSocket
        NotificationDTO dto = notificationMapper.toDTO(notification);
        log.info("📦 Notification DTO: {}", dto);

        // B4: Gửi WebSocket về client theo username
        log.info("📤 Sending to WebSocket user '{}' on /queue/private", username);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/private",
                dto
        );
    }

    @Override
    public void notifyAllAdmins(String title, String content) {
        List<User> admins = userRepository.findAllByRole_Name(RoleName.ADMIN);
        log.info("🔔 Notify {} admins: {}", admins.size(), content);
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setToUser(admin);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);

            messagingTemplate.convertAndSendToUser(
                    admin.getUsername(),
                    "/queue/private",
                    notification
            );
        }
    }

    public Page<Notification> getNotificationsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findAllByToUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Notification> getTopNNotificationsByUser(Long userId, int limit) {
        Pageable topN = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return notificationRepository.findByToUser_Id(userId, topN).getContent();
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}