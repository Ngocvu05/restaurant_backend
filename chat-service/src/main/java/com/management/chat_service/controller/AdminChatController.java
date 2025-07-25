package com.management.chat_service.controller;

import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/join/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId,
                                      @RequestHeader("X-User-Id") Long adminId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy room"));

        if (room.getAdminId() != null && !room.getAdminId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Phòng này đang được xử lý bởi admin khác");
        }

        room.setAdminId(adminId);
        chatRoomRepository.save(room);

        // ✅ send private notification to the admin joining the room
        messagingTemplate.convertAndSendToUser(
                String.valueOf(adminId),
                "/queue/alerts",
                "✅ Bạn vừa nhận xử lý phòng " + roomId
        );

        // send notification to all admins
        messagingTemplate.convertAndSend("/topic/admin/notify",
                "Phòng " + roomId + " đã được nhận xử lý bởi Admin " + adminId);

        return ResponseEntity.ok("Bạn đã nhận xử lý phòng " + roomId);
    }

    @PutMapping("/resolve/{roomId}")
    public ResponseEntity<?> resolveRoom(@PathVariable String roomId,
                                         @RequestHeader("X-User-Id") Long adminId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy room"));

        if (!adminId.equals(room.getAdminId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền đánh dấu phòng này");
        }

        room.setResolved(true);
        chatRoomRepository.save(room);

        // Gửi thông báo tới tất cả admin
        messagingTemplate.convertAndSend("/topic/admin/notify",
                "Phòng " + roomId + " đã được xử lý hoàn tất bởi Admin " + adminId);

        return ResponseEntity.ok("Phòng đã được đánh dấu là đã xử lý");
    }

    @GetMapping("/rooms/all")
    public ResponseEntity<?> getAllChatRooms() {
        log.info("📥 Fetching all chat rooms for admin");
        return ResponseEntity.ok(chatRoomService.getAllRoomsForAdmin());
    }
}
