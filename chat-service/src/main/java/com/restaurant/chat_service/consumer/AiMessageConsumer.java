package com.restaurant.chat_service.consumer;

import com.restaurant.chat_service.config.RabbitMQConfig;
import com.restaurant.chat_service.dto.ChatMessageDTO;
import com.restaurant.chat_service.dto.ChatMessageResponse;
import com.restaurant.chat_service.mapper.IChatMessageMapper;
import com.restaurant.chat_service.model.ChatMessage;
import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatMessageRepository;
import com.restaurant.chat_service.repository.ChatRoomRepository;
import com.restaurant.chat_service.service.IChatAIService;
import com.restaurant.chat_service.status.MessageType;
import com.restaurant.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiMessageConsumer {
    private final IChatAIService chatAIService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatMessageMapper chatMessageMapper;
    private final SimpMessagingTemplate messagingTemplate;
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

            ChatMessage aiMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .senderId(chatRoom.getUserId())
                    .senderName("AI Assistant")
                    .content(aiResponse)
                    .type(MessageType.AI_RESPONSE)
                    .senderType(SenderType.ASSISTANT)
                    .isRead(false)
                    .isAiGenerated(true)
                    .build();

            ChatMessage saved = chatMessageRepository.save(aiMessage);
            ChatMessageDTO dto = chatMessageMapper.toDTO(saved);

            // Send a response to RabbitMQ for further processing
            ChatMessageResponse response = ChatMessageResponse.builder()
                    .response(aiResponse)
                    .sessionId(chatRoom.getSessionId())
                    .userId(chatRoom.getUserId())
                    .chatRoomId(chatRoom.getId())
                    .build();
            log.info("📌 AIMessageConsumer -  ChatRoom sessionId: {}", chatRoom.getSessionId());
            log.info("📤 AIMessageConsumer - Gửi message tới queue chat.response: {}", response);
            rabbitTemplate.convertAndSend("chat.response", response);

            messagingTemplate.convertAndSend("/topic/room/" + roomId, dto);
        } catch (Exception e) {
            log.error("❌ AIMessageConsumer -  Error handling AI message: ", e);
        }
    }
}
