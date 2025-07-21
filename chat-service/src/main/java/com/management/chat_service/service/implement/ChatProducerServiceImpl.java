package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.service.IChatAIService;
import com.management.chat_service.service.IChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.management.chat_service.config.RabbitMQConfig.AI_ROUTING_KEY;
import static com.management.chat_service.config.RabbitMQConfig.CHAT_EXCHANGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducerServiceImpl implements IChatProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final IChatAIService chatAIService;

    @Override
    public void sendToAI(String roomId, String content) {
        // Prepare payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomId", roomId);
        payload.put("content", content);

        log.info("🚀 ChatProducerService - Gửi message tới AI queue - roomId: {}, content: {}", roomId, content);

        // Send Object to RabbitMQ
        rabbitTemplate.convertAndSend(
                CHAT_EXCHANGE,
                AI_ROUTING_KEY,
                payload
        );
    }

    @Override
    public void sendMessageToChatQueue(ChatMessageRequest request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, request);
    }

    @Override
    public void handleGuestAIMessage(String sessionId, String content) {
        chatAIService.ask(content);
    }
}
