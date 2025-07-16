package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.config.RabbitMQConfig;
import com.restaurant.chat_service.dto.ChatMessageRequest;
import com.restaurant.chat_service.dto.ChatMessageResponse;
import com.restaurant.chat_service.model.ChatMessage;
import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatMessageRepository;
import com.restaurant.chat_service.service.IChatAIService;
import com.restaurant.chat_service.service.IChatConsumer;
import com.restaurant.chat_service.service.IChatProducerService;
import com.restaurant.chat_service.service.IChatRoomService;
import com.restaurant.chat_service.status.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumerImpl implements IChatConsumer {
    private final IChatAIService chatAIService;
    private final RabbitTemplate rabbitTemplate;
    private final IChatRoomService chatRoomService;
    private final IChatProducerService chatProducerService;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    //@RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
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

    @Override
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consumeMessage(ChatMessageRequest request) {
        log.info("✅ ChatConsumer - Nhận request từ user: {}", request);

        ChatRoom chatRoom = chatRoomService.getOrCreateRoom(request);

        // Gửi cho AI
        chatProducerService.sendToAI(chatRoom.getRoomId(), request.getMessage());

        // If userid is existed, store Db
        if (request.getUserId() != null) {
            ChatMessage message = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .senderId(request.getUserId())
                    .senderName("User " + request.getUserId())
                    .content(request.getMessage())
                    .senderType(request.getSenderType())
                    .type(MessageType.TEXT)
                    .isRead(false)
                    .isAiGenerated(false)
                    .build();
            chatMessageRepository.save(message);
        }
    }
}
