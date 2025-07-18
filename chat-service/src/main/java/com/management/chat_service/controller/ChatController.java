package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.service.IChatMessageService;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final IChatProducerService chatProducerService;
    private final IChatRoomService chatRoomService;
    private final IChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendChat(@RequestBody ChatMessageRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        log.info(" ChatController - Received message: {}", request);

        if (userIdHeader != null) {
            request.setUserId(Long.parseLong(userIdHeader));
        }

        chatProducerService.sendMessageToChatQueue(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestHeader("X-User-Id") Long userId) {
        List<ChatRoom> rooms = chatRoomService.getRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public Page<ChatMessageDTO> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return chatMessageService.getMessagesByRoomId(roomId, page, size);
    }
}
