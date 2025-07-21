package com.management.chat_service.service.handler;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiMessageHandler implements IChatMessageHandler {
    private final RabbitTemplate rabbitTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public AiMessageHandler(RabbitTemplate rabbitTemplate, ChatMessageRepository chatMessageRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public boolean supports(SenderType senderType) {
        return senderType == SenderType.AI;
    }

    @Override
    public ChatMessage handleMessage(ChatRoom chatRoom, String senderName, Long senderId, String content) {
        // Send a message to AI via RabbitMQ
        Map<String, Object> payload = Map.of(
                "roomId", chatRoom.getRoomId(),
                "senderName", senderName,
                "senderId", senderId,
                "content", content
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.AI_ROUTING_KEY, payload);

        // Create a placeholder message to indicate AI is processing
        ChatMessage placeholder = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .senderName(senderName)
                .content("⏳ AI is processing...")
                .type(MessageType.AI_RESPONSE)
                .senderType(SenderType.AI)
                .isRead(false)
                .isAiGenerated(true)
                .build();

        return chatMessageRepository.save(placeholder);
    }
}

