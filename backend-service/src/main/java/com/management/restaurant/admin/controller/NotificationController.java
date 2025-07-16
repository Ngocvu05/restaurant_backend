package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.NotificationDTO;
import com.management.restaurant.admin.mapper.NotificationMapper;
import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.model.Notification;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/notifications/")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal UserPrincipal user) {

        if (limit != null) {
            List<Notification> topN = notificationService.getTopNNotificationsByUser(user.getId(), limit);
            List<NotificationDTO> dtoList = topN.stream()
                    .map(notificationMapper::toDTO)
                    .toList();
            Page<NotificationDTO> dtoPage = new PageImpl<>(dtoList); // ✅ wrap List to Page
            return ResponseEntity.ok(dtoPage);
        }

        Page<Notification> notifications = notificationService.getNotificationsByUser(user.getId(), page, size);
        Page<NotificationDTO> dtoPage = notifications.map(notificationMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestParam String content) {
        notificationService.sendNotificationToUser(userId, content);
        return ResponseEntity.ok("Notification sent");
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Marked as read");
    }

    // Gửi broadcast đến tất cả user đang subscribe
    @PostMapping("/notify/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
        return ResponseEntity.ok("Đã gửi broadcast");
    }

    // Gửi thông báo riêng đến 1 user (ví dụ: username = "admin")
    @PostMapping("/notify/private/{username}")
    public ResponseEntity<String> sendToUser(@PathVariable String username, @RequestBody String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
        return ResponseEntity.ok("Đã gửi riêng");
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<?> testNotification(@RequestParam String username, @RequestParam String title, @RequestParam String message) {
        log.info("🚀 [testNotification] Request to send test notification to: {} | message: {}", username, message);

        // Kiểm tra user tồn tại
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("❌ User not found with username: " + username));
        log.info("👤 Found user ID: {}", user.getId());
        log.info("📤 Sending message to WebSocket user '{}'", username);
        // Gọi service tạo notification và tự gửi WebSocket trong đó
        notificationService.createNotification(user.getUsername(), title, message);

        // Trả về phản hồi
        return ResponseEntity.ok("✅ Test notification sent to user '" + username + "'");
    }

}
