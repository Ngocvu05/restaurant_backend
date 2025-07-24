package com.management.chat_service.controller;

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

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getChatRooms(@PathVariable Long userId) {
        log.info("📥 Get chat rooms for userId = {}", userId);
        Map<String, Object> response = new HashMap<>();
        response.put("rooms", chatRoomService.getAllRooms(userId));
        return ResponseEntity.ok(response);
    }

}
