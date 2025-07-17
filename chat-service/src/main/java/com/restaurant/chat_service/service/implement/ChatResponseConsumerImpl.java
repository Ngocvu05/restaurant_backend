package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.dto.ChatMessageDTO;
import com.restaurant.chat_service.dto.ChatMessageResponse;
import com.restaurant.chat_service.mapper.IChatMessageMapper;
import com.restaurant.chat_service.model.ChatMessage;
import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatMessageRepository;
import com.restaurant.chat_service.repository.ChatRoomRepository;
import com.restaurant.chat_service.service.IChatResponseConsumer;
import com.restaurant.chat_service.service.IChatWebSocketService;
import com.restaurant.chat_service.status.MessageType;
import com.restaurant.chat_service.status.SenderType;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final IChatMessageMapper chatMessageMapper;

    @Override
    @RabbitListener(queues = "chat.response")
    public void receiveAIResponse(ChatMessageResponse response) {
        try {
            log.info("📩 ChatResponseConsumer - Nhận phản hồi từ AI: {}", response);

            Long chatRoomId = response.getChatRoomId();
            if (chatRoomId == null) {
                log.error("❌ ChatResponseConsumer - ChatRoomId trong response bị null");
                return;
            }

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("Not found chat room with ChatRoomId: " + chatRoomId));

            ChatMessage aiMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .senderId(response.getUserId())
                    .senderName("AI Assistant")
                    .senderType(SenderType.AI)
                    .type(MessageType.TEXT)
                    .content(response.getResponse())
                    .isAiGenerated(true)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            if(response.getUserId() != null) {
                chatMessageRepository.save(aiMessage);
            }

            log.info("💾 ChatResponseConsumer - Đã lưu phản hồi AI vào DB");
            ChatMessageDTO dto = chatMessageMapper.toDTO(aiMessage);
            // 2. send response to WebSocket
            webSocketService.sendMessageToRoom(response.getSessionId(), response);
            log.info("📤 ChatResponseConsumer - Đã gửi phản hồi AI qua WebSocket đến room {}", response.getSessionId());
            messagingTemplate.convertAndSend("/topic/room/" + response.getSessionId(), dto);
        } catch (Exception e) {
            log.error("❌ ChatResponseConsumer - Lỗi khi xử lý phản hồi AI", e);
        }

    }
}
