package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.model.ChatRoom;
import com.restaurant.chat_service.repository.ChatRoomRepository;
import com.restaurant.chat_service.service.IChatRoomService;
import com.restaurant.chat_service.status.ChatRoomStatus;
import com.restaurant.chat_service.status.ChatRoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                .type(ChatRoomType.AI)
                .status(ChatRoomStatus.ACTIVE)
                .description("AI assistant chat")
                .build();

        return chatRoomRepository.save(chatRoom);
    }
}
