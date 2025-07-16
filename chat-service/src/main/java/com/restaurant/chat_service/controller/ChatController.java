package com.restaurant.chat_service.controller;

import com.restaurant.chat_service.config.RabbitMQConfig;
import com.restaurant.chat_service.dto.ChatMessageRequest;
import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatRoomRepository;
import com.restaurant.chat_service.service.IChatAIService;
import com.restaurant.chat_service.service.IChatProducerService;
import com.restaurant.chat_service.status.ChatRoomStatus;
import com.restaurant.chat_service.status.ChatRoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatRoomRepository chatRoomRepository;
    private final IChatProducerService chatProducerService;

    @PostMapping("/send")
    public ResponseEntity<?> sendChatMessage(@RequestBody ChatMessageRequest request) {
        log.info("✅ ChatController - Received request from Postman {}", request);

        try {
            // 1. Create or find chat room
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(request.getChatRoomId())
                    .orElseGet(() -> {
                        ChatRoom newRoom = ChatRoom.builder()
                                .roomId(request.getChatRoomId())
                                .sessionId(request.getSessionId())
                                .userId(request.getUserId())
                                .name("Chat with AI")
                                .type(ChatRoomType.AI)
                                .description("AI assistant chat")
                                .status(ChatRoomStatus.ACTIVE)
                                .build();
                        return chatRoomRepository.save(newRoom);
                    });

            // 2. send message to AI queue
            chatProducerService.sendToAI(chatRoom.getRoomId(), request.getMessage());

            log.info("🎯 ChatController - Gửi message tới AI queue thành công: {}", request.getMessage());

            return ResponseEntity.ok("✅ Tin nhắn đã được gửi đến AI và đang chờ phản hồi.");
        } catch (Exception e) {
            log.error("❌ ChatController - Lỗi khi gửi tin nhắn đến AI", e);
            return ResponseEntity.status(500).body("❌ Có lỗi xảy ra khi gửi tin nhắn");
        }
    }
}
