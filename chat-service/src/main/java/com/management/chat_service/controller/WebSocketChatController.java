package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
    private final IChatProducerService chatProducerService;
    private final ChatRoomRepository chatRoomRepository;
    private final IChatRoomMapper chatRoomMapper;
    private final IChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(ChatMessageRequest request,
                                       @Header("simpSessionAttributes") Map<String, Object> sessionAttrs,
                                       @Header(name = "X-User-Id", required = false) Optional<String> userIdHeader) {
        log.info("📨 WebSocket message received: {}", request);
        String userId = (String) sessionAttrs.get("userId");
        // Sync userId from header if available
        if (userId != null) {
            request.setUserId(Long.parseLong(userId));
        }

        chatProducerService.sendMessageToChatQueue(request);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestHeader("X-User-Id") Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);
        List<ChatRoomDTO> roomDtos = rooms.stream()
                .map(chatRoomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roomDtos);
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<?> getMessages(@PathVariable String roomId,
                                         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (room.getUserId() != null && !room.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền truy cập phòng này");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room);
        return ResponseEntity.ok(messages.stream().map(chatMessageMapper::toDTO).toList());
    }
}
