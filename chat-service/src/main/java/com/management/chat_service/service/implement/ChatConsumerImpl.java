package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.service.IChatConsumer;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumerImpl implements IChatConsumer {
    private final IChatRoomService chatRoomService;
    private final IChatProducerService chatProducerService;
    private final ChatMessageRepository chatMessageRepository;
    private final IGuestChatService guestChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    //@RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE, containerFactory = "retryContainerFactory")
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consumeMessage(ChatMessageRequest request) {
        log.info("✅ ChatConsumer - Nhận request từ user: {}", request);
        //handel for user already logged in
        if (request.getUserId() != null) {
            ChatRoom chatRoom = chatRoomService.getOrCreateRoom(request);
            log.info("🧾 Room info: id={}, roomId={}, userId={}", chatRoom.getId(), chatRoom.getRoomId(), chatRoom.getUserId());
            // Send message to AI
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
                log.info("✅ Đã lưu message của user {} vào DB", request.getUserId());
            }
        // handel for guest user
        }else if (request.getSenderType() == SenderType.GUEST) {
            guestChatService.saveGuestMessageToRedis(request);
            chatProducerService.handleGuestAIMessage(request);
            log.info("👤 Guest message - không lưu DB, chỉ gửi AI, sessionId: {}, content: {}", request.getSessionId(), request.getMessage());
        }else {
            log.warn("⚠️ ChatConsumer - Nhận request không hợp lệ: {}", request);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.GUEST_CHAT_QUEUE)
    public void handleGuestMessage(Map<String, Object> payload) {
        log.info("📩 GuestChatConsumer - {}", payload);
        String sessionId = (String) payload.get("sessionId");
        String responseMessage = (String) payload.get("response");
        if (sessionId == null || responseMessage == null) {
            log.warn("⚠️ GuestChatConsumer - Invalid guest message: {}", payload);
            return;
        }

        ChatMessageRequest request = ChatMessageRequest.builder()
                .sessionId(sessionId)
                .message(responseMessage)
                .senderType(SenderType.GUEST)
                .build();
        // Save guest message to Redis
        guestChatService.saveGuestMessageToRedis(request);

        //Send AI response to guest
        chatProducerService.handleGuestAIMessage(request);
        messagingTemplate.convertAndSend("/topic/guest/" + sessionId, responseMessage);
    }
}
