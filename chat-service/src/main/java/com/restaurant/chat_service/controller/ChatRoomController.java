package com.restaurant.chat_service.controller;

import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
