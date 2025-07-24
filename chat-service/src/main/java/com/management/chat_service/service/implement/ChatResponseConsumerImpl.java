package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatResponseConsumer;
import com.management.chat_service.service.IChatWebSocketService;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatResponseConsumerImpl implements IChatResponseConsumer {
    private final IChatWebSocketService webSocketService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final IGuestChatService guestChatService;

    @Override
    @RabbitListener(queues = RabbitMQConfig.RESPONSE_QUEUE)
    public void receiveAIResponse(ChatMessageResponse response) {
        try {
            log.info("📥 ChatResponseConsumer - Nhận phản hồi từ AI: {}", response);

            if (response.getSessionId() != null && response.getUserId() == null) {
                log.info("📥 ChatResponseConsumer - Store response message to Redis: {}", response);
                // process for a guest user - save message to redis and send to websocket
                guestChatService.handleAIResponse(response);
                return;
            }

            // process for logged-in user
            if (response.getUserId() != null) {
                Long chatRoomId = response.getChatRoomId();
                if (chatRoomId == null) {
                    log.error("❌ ChatResponseConsumer - Thiếu chatRoomId trong response");
                    return;
                }
                log.info("📥 ChatResponseConsumer - Store response message to DB: {}", response);
                ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                        .orElseThrow(() -> new IllegalArgumentException("Not found chatRoomId: " + chatRoomId));

                ChatMessage message = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .senderId(response.getUserId())
                        .senderName(response.getSenderType() == SenderType.ADMIN ? "Admin" : "AI Assistant")
                        .senderType(response.getSenderType())
                        .type(MessageType.TEXT)
                        .content(response.getResponse())
                        .isAiGenerated(response.getSenderType() == SenderType.AI)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                chatMessageRepository.save(message);
                webSocketService.sendMessageToRoom(response.getSessionId(), response);
            }
        } catch (Exception e) {
            log.error("❌ ChatResponseConsumer - Lỗi xử lý phản hồi AI", e);
        }

    }
}
