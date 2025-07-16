package com.restaurant.chat_service.service;

import com.restaurant.chat_service.config.RabbitMQConfig;
import com.restaurant.chat_service.dto.ChatMessageRequest;
import com.restaurant.chat_service.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatConsumer {
    private final IChatAIService chatAIService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void receiveMessage(ChatMessageRequest request) {
        System.out.println("✅ChatConsumer -  Nhận request từ user: " + request);
        String reply = chatAIService.sendToAI(request.getMessage());

        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .response(reply)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, "chat.response", response);
        System.out.println("🎯 Gửi phản hồi về queue chat.response: " + reply);
    }
}
