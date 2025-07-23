package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.service.IChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducerServiceImpl implements IChatProducerService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendToAI(ChatMessageRequest request) {
        log.info("🚀 ChatProducerService - Gửi ChatMessageRequest tới AI queue - {} ", request);

        // Send Object to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.AI_ROUTING_KEY, request);
    }

    @Override
    public void sendMessageToChatQueue(ChatMessageRequest request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, request);
    }

    @Override
    public void handleGuestAIMessage(ChatMessageRequest request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.AI_ROUTING_KEY, request);
        log.info("🚀 GuestChatService - Sent guest message to AI: {}", request.getMessage());
    }
}
