package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.status.ChatRoomType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducerServiceImpl implements IChatProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public void sendToAI(ChatMessageRequest request) {
        log.info("🚀 ChatProducerService - Gửi ChatMessageRequest tới AI queue - {} ", request);
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

    @Override
    public void sendMessageToUser(ChatMessageRequest request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.USER_TO_USER_ROUTING_KEY, request);
    }

    @Override
    public void sendMessageToChatQueue_v2(ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findByRoomId(request.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with ID: " + request.getChatRoomId()));
        //handle for first time admin joins the room
        if(request.getSenderType() == SenderType.ADMIN) {
            log.info("🚀 Admin {} is joining the chat room {}", request, room.getRoomId());
            room.setType(ChatRoomType.PRIVATE);
            chatRoomRepository.save(room);
        }

        switch (room.getType()) {
            case AI_SUPPORT:
                log.info("🚀 Routing message to AI queue: {}", request);
                rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, request);
                break;
            case PRIVATE:
            case GROUP:
                log.info("🚀 Routing message to User-User queue: {}", request);
                rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.USER_TO_USER_ROUTING_KEY, request);
                break;
            default:
                log.error("Unhandled chat room type: {}", room.getType());
                break;
        }
    }
}
