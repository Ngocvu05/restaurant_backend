package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {
    private final IChatRoomService chatRoomService;
    private final IChatRoomMapper chatRoomMapper;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getChatRooms(@PathVariable Long userId) {
        log.info("📥 Get chat rooms for userId = {}", userId);
        Map<String, Object> response = new HashMap<>();
        response.put("rooms", chatRoomService.getAllRooms(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Create or retrieve a private chat room between the current user and another user.
     * @param targetUserId  ID of the user to chat with.
     * @param currentUserId ID of the currently logged-in user (taken from the header or security context).
     * @return ChatRoomDTO of the chat room.
     */
    @PostMapping("/private")
    public ResponseEntity<ChatRoomDTO> getOrCreatePrivateRoom(
            @RequestParam Long targetUserId,
            @RequestHeader("X-User-Id") Long currentUserId) {

        ChatRoom room = chatRoomService.getOrCreatePrivateRoom(currentUserId, targetUserId);
        return ResponseEntity.ok(chatRoomMapper.toDTO(room));
    }
}