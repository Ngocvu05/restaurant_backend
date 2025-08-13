package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatMessageService;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/chat")
public class AdminChatController {
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final IChatRoomService chatRoomService;
    private final IChatMessageService chatMessageService;
    private final IChatProducerService chatProducerService;

    @PostMapping("/join/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId,
                                      @RequestHeader("X-User-Id") Long adminId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        log.info("📥 Admin {} joining the chat room {}", adminId, roomId);
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Not found room"));

        if (room.getAdminId() != null && !room.getAdminId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This room is being handled by another admin.");
        }

        room.setAdminId(adminId);
        chatRoomRepository.save(room);
        Page<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomId(roomId, page, size);

        // send private notification to the admin joining the room
        messagingTemplate.convertAndSendToUser(
                String.valueOf(adminId),
                "/queue/alerts",
                "✅ You have just taken over handling the room " + roomId
        );

        // send notification to all admins
        messagingTemplate.convertAndSend("/topic/admin/notify",
                "Phòng " + roomId + " đã được nhận xử lý bởi Admin " + adminId);

        return ResponseEntity.ok(messages);
    }

    @PutMapping("/resolve/{roomId}")
    public ResponseEntity<?> resolveRoom(@PathVariable String roomId,
                                         @RequestHeader("X-User-Id") Long adminId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Not found room"));

        if (!adminId.equals(room.getAdminId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to mark this room.");
        }

        room.setResolved(true);
        chatRoomRepository.save(room);

        // Gửi thông báo tới tất cả admin
        messagingTemplate.convertAndSend("/topic/admin/notify",
                "Phòng " + roomId + " đã được xử lý hoàn tất bởi Admin " + adminId);

        return ResponseEntity.ok("The room has been marked as resolved.");
    }

    @GetMapping("/rooms/all")
    public ResponseEntity<?> getAllChatRooms() {
        log.info("📥 Fetching all chat rooms for admin");
        return ResponseEntity.ok(chatRoomService.getAllRoomsForAdmin());
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendChat(@RequestBody ChatMessageRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        log.info(">>> AdminChatController - Received message: {}", request);
        try {
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                request.setUserId(Long.parseLong(userIdHeader));
                log.info(">>> AdminChatController - Set userId: {}", request.getUserId());
            }

            if (request.getSenderType() == null) {
                request.setSenderType(SenderType.ADMIN);
            }

            chatProducerService.sendMessageToChatQueue_v2(request);
            log.info(">>> AdminChatController - Message sent to queue successfully");

            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Message sent\"}");
        } catch (Exception e) {
            log.error(">>> AdminChatController - Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"Failed to send message\"}");
        }
    }
}