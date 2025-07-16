package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.service.IChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.restaurant.chat_service.config.RabbitMQConfig.AI_ROUTING_KEY;
import static com.restaurant.chat_service.config.RabbitMQConfig.CHAT_EXCHANGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducerServiceImpl implements IChatProducerService {
    private final RabbitTemplate rabbitTemplate;
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
}
