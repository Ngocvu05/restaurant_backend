package com.management.chat_service.mapper.implement;

import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.status.ChatRoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatRoomMapperImpl implements IChatRoomMapper {
    private final IChatMessageMapper chatMessageMapper;
    @Override
    public ChatRoomDTO toDTO(ChatRoom chatRoom) {
        if (chatRoom == null) return null;

        ChatMessage lastMessage = chatRoom.getMessages() != null && !chatRoom.getMessages().isEmpty()
                ? chatRoom.getMessages().get(chatRoom.getMessages().size() - 1)
                : null;

        long unreadCount = chatRoom.getMessages() != null
                ? chatRoom.getMessages().stream().filter(m -> !Boolean.TRUE.equals(m.getIsRead())).count()
                : 0;

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getName())
                .userId(chatRoom.getUserId())
                .description(chatRoom.getDescription())
                .roomType(chatRoom.getType())
                .isActive(chatRoom.getStatus())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .unreadCount(unreadCount)
                .lastMessage(chatMessageMapper.toDTO(lastMessage))
                .build();
    }

    @Override
    public ChatRoom toEntity(ChatRoomDTO dto) {
        if (dto == null) return null;

        return ChatRoom.builder()
                .id(dto.getId())
                .roomId(dto.getRoomId())
                .name(dto.getRoomName())
                .userId(dto.getUserId())
                .description(dto.getDescription())
                .type(dto.getRoomType())
                .status(dto.getIsActive() != null ? dto.getIsActive() : ChatRoomStatus.ACTIVE)
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .updatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}
