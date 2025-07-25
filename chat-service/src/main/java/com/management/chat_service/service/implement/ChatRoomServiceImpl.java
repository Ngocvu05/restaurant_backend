package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements IChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final IChatRoomMapper chatRoomMapper;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public ChatRoom getOrCreateRoom(ChatMessageRequest request) {
        String sessionId = request.getSessionId();
        Long userId = request.getUserId();

        return chatRoomRepository.findByUserIdAndSessionId(userId, sessionId)
                .orElseGet(() -> {
                    ChatRoom newRoom = createRoomFromRequest(request);
                    return chatRoomRepository.save(newRoom);
                });
    }

    private ChatRoom createRoomFromRequest (ChatMessageRequest request) {
        return ChatRoom.builder()
                .roomId(request.getChatRoomId())
                .name("Chat with AI" + LocalDateTime.now())
                .type(ChatRoomType.AI_SUPPORT)
                .description("AI assistant chat " + LocalDateTime.now())
                .status(ChatRoomStatus.ACTIVE)
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .build();
    }

    @Override
    @Transactional
    public void convertSessionToUser(String sessionId, Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findBySessionIdAndUserIdIsNull(sessionId);
        for (ChatRoom room : rooms) {
            room.setUserId(userId);
            chatRoomRepository.save(room);
        }
        log.info("✅ convertSessionToUser - Đã gán {} room(s) từ session {} sang userId {}", rooms.size(), sessionId, userId);
    }

    @Override
    public List<ChatRoom> getRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    public List<ChatRoomDTO> getAllRooms(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);
        if (rooms == null || rooms.isEmpty()) {
            return List.of();
        }

        return rooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);
                    if (lastMessage != null) {
                        room.setMessages(List.of(lastMessage));
                    } else {
                        room.setMessages(List.of());
                    }
                    return chatRoomMapper.toDTO(room);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomDTO> getAllRoomsForAdmin() {
        List<ChatRoom> rooms = chatRoomRepository.findAll();
        if (rooms.isEmpty()) {
            return List.of();
        }
        log.info(">>> getAllRoomsForAdmin - Fetching all chat rooms for admin");
        return rooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);
                    if (lastMessage != null) {
                        room.setMessages(List.of(lastMessage));
                    } else {
                        room.setMessages(List.of());
                    }
                    return chatRoomMapper.toDTO(room);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
