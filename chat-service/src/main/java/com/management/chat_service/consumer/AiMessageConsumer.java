package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.service.IAIWorker;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiMessageConsumer {
    private final IAIWorker aiWorker;
    private final IGuestChatService guestChatService;

    @RabbitListener(queues = RabbitMQConfig.AI_QUEUE)
    public void handleAiMessage(Map<String, Object> payload) {
        log.info("🔄 AIMessageConsumer -  Received AI message: {}", payload);
        try {
            String roomId = (String) payload.get("roomId");
            String message = (String) payload.get("message");
            String content = (String) payload.get("content");
            String sessionId = (String) payload.get("sessionId");
            String senderTypeStr = (String) payload.get("senderType");
            SenderType senderType = senderTypeStr != null ? SenderType.valueOf(senderTypeStr) : null;
            content = content != null ? content : message;
            if (roomId == null && message == null) {
                log.warn("⚠️ AIMessageConsumer - Payload thiếu roomId hoặc content: {}", payload);
                return;
            }
            if (sessionId != null && senderType == SenderType.GUEST) {
                log.info("🤖 AI message for GUEST session: {}, {}", sessionId, content);
                ChatMessageResponse response = ChatMessageResponse.builder()
                        .sessionId(sessionId)
                        .response(content)
                        .build();
                guestChatService.handleAIResponse(response);
                return;
            }

            log.info("🔄 AIMessageConsumer -  Received AI message for room {}: {}", roomId, content);
            // Call the AI worker to process the message asynchronously
            aiWorker.processAIMessage(roomId, content);
        } catch (Exception e) {
            log.error("❌ AIMessageConsumer -  Error handling AI message: ", e);
        }
    }

}
