package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.mapper.IChatMessageMapper;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatResponseConsumerImpl implements IChatResponseConsumer {
    private final IChatWebSocketService webSocketService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final IChatMessageMapper chatMessageMapper;
    private final IGuestChatService guestChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @RabbitListener(queues = RabbitMQConfig.RESPONSE_QUEUE)
    public void receiveAIResponse(ChatMessageResponse response) {
        try {
            log.info("📥 ChatResponseConsumer - Nhận phản hồi từ AI: {}", response);

            if (response.getUserId() == null) {
                // process for guest user
                guestChatService.saveGuestResponseToRedis(response);
                webSocketService.sendMessageToRoom(response.getSessionId(), response);
                //messagingTemplate.convertAndSend("/topic/room/" + response.getSessionId(), response);
                return;
            }

            // process for logged-in user
            Long chatRoomId = response.getChatRoomId();
            if (chatRoomId == null) {
                log.error("❌ ChatResponseConsumer - Thiếu chatRoomId trong response");
                return;
            }

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

            ChatMessageDTO dto = chatMessageMapper.toDTO(message);
            webSocketService.sendMessageToRoom(response.getSessionId(), response);
            //messagingTemplate.convertAndSend("/topic/room/" + response.getSessionId(), response);
        } catch (Exception e) {
            log.error("❌ ChatResponseConsumer - Lỗi xử lý phản hồi AI", e);
        }

    }
}
