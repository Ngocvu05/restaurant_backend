package com.management.chat_service.controller;

import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {
    private final IChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChatRoom(@RequestParam Long userId) {
        ChatRoom room = chatRoomService.createNewRoom(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getRoomId());
        response.put("sessionId", room.getSessionId());
        response.put("chatRoomId", room.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getChatRooms(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("rooms", chatRoomService.getRooms(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getChatRooms_v2(@RequestHeader(value = "X-User-Id") Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("rooms", chatRoomService.getRooms(userId));
        return ResponseEntity.ok(response);
    }
}
