package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IChatWebSocketService;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestChatServiceImpl implements IGuestChatService {
    //Private constants and dependencies
    private static final String REDIS_PREFIX = "guest_chat:";
    private static final int TTL_DAYS = 1;
    private static final String AI_MESSAGE_SUFFIX = ":ai_processing";

    private final IChatProducerService chatProducerService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final IChatWebSocketService webSocketService;

    private String getRedisKey(String sessionId) {
        return REDIS_PREFIX + sessionId;
    }

    @Override
    public void handleGuestMessage(GuestChatMessageDTO message) {
        String key = REDIS_PREFIX + message.getSessionId();
        // Create ChatMessageRequest object to send to the producer
        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatRoomId(message.getSessionId()) // sessionId dùng làm roomId
                .sessionId(message.getSessionId())
                .message(message.getContent())
                .senderType(SenderType.GUEST)
                .timestamp(message.getCreatedAt())
                .build();
        chatProducerService.handleGuestAIMessage(request);
        // Push request to Redis
        redisTemplate.opsForList().rightPush(key,request);
        redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS); //TTL: Set expiration to 1 day
    }

    @Override
    public void handleAIResponse(ChatMessageResponse response) {
        // Push response to Redis (optional)
        saveGuestResponseToRedis(response);

        // Send response to Response Queue -> this process run on class AIWorkerImpl
        //rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.RESPONSE_ROUTING_KEY, response);
        webSocketService.sendMessageToRoom(response.getSessionId(), response);
    }

    @Override
    public List<Object> getGuestMessages(String sessionId) {
        return redisTemplate.opsForList().range(getRedisKey(sessionId), 0, -1);
    }

    @Override
    @Transactional
    public void migrateToDatabase(String sessionId, Long userId) {
        String key = REDIS_PREFIX + sessionId;
        List<Object> messages = getGuestMessages(sessionId);
        log.info("Migrating guest chat messages from Redis to database for session: {}, message: {}", sessionId, messages);
        if (messages == null || messages.isEmpty()) return;

        // Create or find chat room
        ChatRoom room = chatRoomRepository.findBySessionId(sessionId)
                .orElseGet(() -> chatRoomRepository.save( ChatRoom.builder()
                        .roomId(UUID.randomUUID().toString()) // Ceate a new room ID
                        .sessionId(sessionId)
                        .userId(userId)
                        .name("Chat Room for Session " + sessionId)
                        .type(ChatRoomType.AI_SUPPORT)
                        .status(ChatRoomStatus.ACTIVE) // default status
                        .description("Chat room created for session " + sessionId)
                        .createdAt(LocalDateTime.now())
                        .build()));
        log.info("Chat room for session {}: {}", sessionId, room);

        for (Object raw : messages) {
            if (raw instanceof ChatMessageRequest request) {
                ChatMessage msg = ChatMessage.builder()
                        .chatRoom(room)
                        .senderId(userId)
                        .senderName("User " + userId)
                        .content(request.getMessage())
                        .senderType(SenderType.USER)
                        .type(MessageType.TEXT)
                        .isAiGenerated(false)
                        .isRead(false)
                        .build();
                chatMessageRepository.save(msg);
            log.info("Saved user Chat message request to database: {}", msg);

            } else if (raw instanceof ChatMessageResponse response) {
                ChatMessage msg = ChatMessage.builder()
                        .chatRoom(room)
                        .senderId(userId)
                        .senderName("AI")
                        .content(response.getResponse())
                        .senderType(SenderType.AI)
                        .type(MessageType.TEXT)
                        .isAiGenerated(true)
                        .isRead(true)
                        .build();
                chatMessageRepository.save(msg);
                log.info("Saved user Chat message response to database: {}", msg);
            }
        }
        // Clean up Redis
        redisTemplate.delete(key);
    }

    @Override
    public void saveGuestMessageToRedis(ChatMessageRequest request) {
        redisTemplate.opsForList().rightPush(getRedisKey(request.getSessionId()), request);
    }

    @Override
    public void saveGuestResponseToRedis(ChatMessageResponse response) {
        redisTemplate.opsForList().rightPush(getRedisKey(response.getSessionId()), response);
    }

}
