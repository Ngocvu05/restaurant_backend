package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GuestChatServiceImpl implements IGuestChatService {
    private final IChatProducerService chatProducerService;
    private static final String PREFIX = "guest_chat:";

    @Autowired
    private RedisTemplate<String, GuestChatMessageDTO> redisTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Override
    public void handleGuestMessage(GuestChatMessageDTO message) {
        String key = PREFIX + message.getSessionId();
        chatProducerService.handleGuestAIMessage(message.getSessionId(), message.getContent());
        redisTemplate.opsForList().rightPush(key,message);
        redisTemplate.expire(key, 1, TimeUnit.DAYS); //TTL: Set expiration to 1 day
    }


    @Override
    public List<GuestChatMessageDTO> getMessages(String sessionId) {
        String key = PREFIX + sessionId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public void migrateToDatabase(String sessionId, Long userId) {
        String key = PREFIX + sessionId;
        List<GuestChatMessageDTO> messages = redisTemplate.opsForList().range(key, 0, -1);

        if (messages == null || messages.isEmpty()) return;

        // Create or find chat room
        ChatRoom room = chatRoomRepository.findBySessionId(sessionId)
                .orElseGet(() -> chatRoomRepository.save( ChatRoom.builder()
                        .roomId(UUID.randomUUID().toString()) // Tạo roomId ngẫu nhiên
                        .sessionId(sessionId)
                        .userId(userId)
                        .name("Chat Room for Session " + sessionId)
                        .type(ChatRoomType.AI_SUPPORT)
                        .status(ChatRoomStatus.ACTIVE) // default trong builder nhưng nên rõ ràng
                        .description("Chat room created for session " + sessionId)
                        .createdAt(LocalDateTime.now())
                        .build()));

        for (GuestChatMessageDTO msg : messages) {
            ChatMessage entity = ChatMessage.builder()
                    .chatRoom(room)
                    .content(msg.getContent())
                    .senderType(SenderType.valueOf(msg.getSenderType().name()))
                    .createdAt(msg.getCreatedAt())
                    .build();
            chatMessageRepository.save(entity);
        }

        // Clean up Redis
        redisTemplate.delete(key);
    }
}
