package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.service.IAIWorker;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiMessageConsumer {
    private final IAIWorker aiWorker;

    @RabbitListener(queues = RabbitMQConfig.AI_QUEUE)
    public void handleAiMessage(ChatMessageRequest request) {
        log.info("🤖 AIMessageConsumer - Receive message: {}", request);
        try {
            String sessionId =  request.getSessionId();
            String roomId = request.getChatRoomId();
            String content = request.getMessage();
            String senderTypeStr = request.getSenderType() != null ? request.getSenderType().name() : null;
            SenderType senderType = senderTypeStr != null ? SenderType.valueOf(senderTypeStr) : null;

            if (senderType == SenderType.GUEST && sessionId != null) {
                aiWorker.processGuestMessage(sessionId, content);
                return;
            }

            // Send to the AI worker for processing for the user/admin.
            if (roomId != null) {
                aiWorker.processAIMessage(roomId, content);
            } else {
                log.warn("⚠️ AIMessageConsumer - Missing roomId to process the message: {}", request);
            }

        } catch (Exception e) {
            log.error("❌ AIMessageConsumer - Error processing message from AI queue.", e);
        }
    }
}