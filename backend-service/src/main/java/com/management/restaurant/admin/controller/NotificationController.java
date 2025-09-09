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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/notifications/")
@RequiredArgsConstructor
@Validated
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
        try{
            if (limit != null) {
                Page<Notification> topN = notificationService.getTopNNotificationsByUser(user.getId(), limit);
                Page<NotificationDTO> dtoList = topN.map(notificationMapper::toDTO);
                return ResponseEntity.ok(dtoList);
            }else{
                Page<Notification> notifications = notificationService.getNotificationsByUser(user.getId(), page, size);
                Page<NotificationDTO> dtoPage = notifications.map(notificationMapper::toDTO);
                return ResponseEntity.ok(dtoPage);
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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

    // send notification to all-users subscribe
    @PostMapping("/notify/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
        return ResponseEntity.ok("Successfully");
    }

    // send notification to user
    @PostMapping("/notify/private/{username}")
    public ResponseEntity<String> sendToUser(@PathVariable String username, @RequestBody String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
        return ResponseEntity.ok("Successfully");
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<?> testNotification(@RequestParam String username, @RequestParam String title, @RequestParam String message) {
        log.info("🚀 [testNotification] Request to send test notification to: {} | message: {}", username, message);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("❌ User not found with username: " + username));
        log.info("👤 Found user ID: {}", user.getId());
        log.info("📤 Sending message to WebSocket user '{}'", username);

        notificationService.createNotification(user.getUsername(), title, message);

        return ResponseEntity.ok("✅ Test notification sent to user '" + username + "'");
    }
}