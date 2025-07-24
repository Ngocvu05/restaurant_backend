package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.service.IChatMessageService;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.status.SenderType;
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
        log.info(">>> ChatController - Received message: {}", request);

        try {
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                request.setUserId(Long.parseLong(userIdHeader));
                log.info(">>> ChatController - Set userId: {}", request.getUserId());
            }

            if (request.getSenderType() == null) {
                request.setSenderType(SenderType.USER); // fallback
            }

            chatProducerService.sendMessageToChatQueue(request);
            log.info(">>> ChatController - Message sent to queue successfully");

            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Message sent\"}");

        } catch (Exception e) {
            log.error(">>> ChatController - Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"Failed to send message\"}");
        }
    }


    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        log.info(">>> ChatController - Getting chat history for user: {}", userIdHeader);

        if (userIdHeader == null || userIdHeader.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"User ID required\"}");
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            List<ChatRoom> rooms = chatRoomService.getRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error(">>> ChatController - Error getting chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to get chat history\"}");
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(">>> ChatController - Getting messages for room: {}, page: {}, size: {}", roomId, page, size);

        try {
            Page<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomId(roomId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error(">>> ChatController - Error getting messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to get messages\"}");
        }
    }
}