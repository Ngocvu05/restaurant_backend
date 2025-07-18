package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatAIService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiMessageConsumer {
    private final IChatAIService chatAIService;
    private final ChatRoomRepository chatRoomRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.AI_QUEUE)
    public void handleAiMessage(Map<String, Object> payload) {
        try {
            String roomId = (String) payload.get("roomId");
            String content = (String) payload.get("content");

            if (roomId == null || content == null) {
                log.warn("⚠️ AIMessageConsumer - Payload thiếu roomId hoặc content: {}", payload);
                return;
            }

            log.info("🔄 AIMessageConsumer -  Received AI message for room {}: {}", roomId, content);

            ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            String aiResponse = chatAIService.sendToAI(content);

            // Send a response to RabbitMQ for further processing
            ChatMessageResponse response = ChatMessageResponse.builder()
                    .response(aiResponse)
                    .sessionId(chatRoom.getSessionId())
                    .userId(chatRoom.getUserId())
                    .chatRoomId(chatRoom.getId())
                    .senderType(SenderType.AI)
                    .build();

            log.info("📌 AIMessageConsumer -  ChatRoom sessionId: {}", chatRoom.getSessionId());
            log.info("📤 AIMessageConsumer - Gửi message tới queue chat.response: {}", response);
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_RESPONSE_ROUTING_KEY, response);
        } catch (Exception e) {
            log.error("❌ AIMessageConsumer -  Error handling AI message: ", e);
        }
    }
}
