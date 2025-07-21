package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.service.IAIWorker;
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
            // Call the AI worker to process the message asynchronously
            aiWorker.processAIMessage(roomId, content);
        } catch (Exception e) {
            log.error("❌ AIMessageConsumer -  Error handling AI message: ", e);
        }
    }
}
