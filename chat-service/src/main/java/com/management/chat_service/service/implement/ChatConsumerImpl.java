package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.service.*;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumerImpl implements IChatConsumer {

    private final IChatRoomService chatRoomService;
    private final IChatProducerService chatProducerService;
    private final ChatMessageRepository chatMessageRepository;
    private final IGuestChatService guestChatService;
    private final IChatWebSocketService chatWebSocketService;

    @Override
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consumeMessage(ChatMessageRequest request) {
        log.info("📨 CHAT_QUEUE - Nhận request: {}", request);

        if (request.getUserId() != null) {
            handleLoggedInUserChat(request, true); // Send it to Ai
        } else if (request.getSenderType() == SenderType.GUEST) {
            handleGuestChat(request);
        } else {
            log.warn("❌Can not process CHAT_QUEUE invalid: {}", request);
        }
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.GUEST_CHAT_QUEUE)
    public void handleGuestMessage(ChatMessageRequest request) {
        log.info("📩 GUEST_CHAT_QUEUE - Nhận request: {}", request);

        if (request.getSenderType() != SenderType.GUEST) {
            log.warn("❌ Bỏ qua message vì senderType không phải GUEST: {}", request);
            return;
        }
        handleGuestChat(request);
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.USER_TO_USER_QUEUE)
    public void handleUserToUserMessage(ChatMessageRequest request) {
        log.info("🤝 USER_TO_USER_QUEUE - Nhận request: {}", request);

        if (request.getUserId() != null) {
            handleLoggedInUserChat(request, false); // Không gửi AI
        } else {
            log.warn("❌ USER_TO_USER_QUEUE - Không có userId trong request: {}", request);
        }
    }

    // Shared logic: handle chat của user đã login
    private void handleLoggedInUserChat(ChatMessageRequest request, boolean sendToAI) {
        ChatRoom chatRoom = chatRoomService.getOrCreateRoom(request);
        if (chatRoom.getRoomId() != null) {
            request.setChatRoomId(chatRoom.getRoomId());
        }

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
        log.info("✅ Đã lưu message userId={} vào DB: {}", request.getUserId(), request);

        if (sendToAI) {
            chatProducerService.sendToAI(request);
            log.info("🧠 Gửi message tới AI: {}", request.getMessage());
        } else {
            log.info("📤 Admin chat -  request content: {}",request);
        }
    }

    // Shared logic: handle guest chat
    private void handleGuestChat(ChatMessageRequest request) {
        if (request.getSessionId() == null || request.getMessage() == null) {
            log.warn("⚠️ GUEST_CHAT_QUEUE - Invalid request: {}", request);
            return;
        }

        guestChatService.saveGuestMessageToRedis(request);
        chatProducerService.handleGuestAIMessage(request);
        log.info("✅ GuestChat - Gửi AI và lưu Redis (sessionId={}): {}", request.getSessionId(), request.getMessage());
    }
}
