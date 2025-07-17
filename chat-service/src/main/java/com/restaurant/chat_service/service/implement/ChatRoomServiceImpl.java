package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.dto.ChatMessageRequest;
import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatRoomRepository;
import com.restaurant.chat_service.service.IChatRoomService;
import com.restaurant.chat_service.status.ChatRoomStatus;
import com.restaurant.chat_service.status.ChatRoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements IChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoom createNewRoom(Long userId) {
        // Create a new chat room with a unique session ID and room ID
        String sessionId = UUID.randomUUID().toString();
        String roomId = "room-" + System.currentTimeMillis();

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .name("Chat with AI - " + userId)
                .userId(userId)
                .sessionId(sessionId)
                .type(ChatRoomType.AI_SUPPORT)
                .status(ChatRoomStatus.ACTIVE)
                .description("AI assistant chat")
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoom getOrCreateRoom(ChatMessageRequest request) {
        String roomId = request.getChatRoomId();
        Long userId = request.getUserId();

        return chatRoomRepository.findByRoomId(roomId).orElseGet(() -> {
            // If roomId is not provided, generate a new one
            if (roomId == null || roomId.isBlank()) {
                throw new IllegalArgumentException("Session ID is required to create or get chat room");
            }

            ChatRoom.ChatRoomBuilder builder = ChatRoom.builder()
                    .roomId(roomId)
                    .name("Chat with AI")
                    .type(ChatRoomType.AI_SUPPORT)
                    .description("AI assistant chat")
                    .status(ChatRoomStatus.ACTIVE)
                    .sessionId(request.getSessionId());
            // If userId is provided, set it in the chat room
            if (userId != null) {
                builder.userId(userId);
            }
            return chatRoomRepository.save(builder.build());

        });
    }

    @Override
    public void convertSessionToUser(String sessionId, Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findBySessionIdAndUserId(sessionId, null);
        for (ChatRoom room : rooms) {
            room.setUserId(userId);
            chatRoomRepository.save(room);
        }
    }
}
