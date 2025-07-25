package com.management.chat_service.mapper.implement;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatMessageMapperImpl implements IChatMessageMapper {

    @Override
    public ChatMessageDTO toDTO(ChatMessage message) {
        if (message == null) return null;

        return ChatMessageDTO.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getRoomId())
                .sessionId(message.getChatRoom().getSessionId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .messageType(message.getType())
                .senderType(message.getSenderType())
                .messageStatus(message.getStatus())
                .metadata(message.getMetadata())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public ChatMessage toEntity(ChatMessageDTO dto, ChatRoom chatRoom) {
        if (dto == null) return null;

        return ChatMessage.builder()
                .id(dto.getId())
                .chatRoom(chatRoom)
                .senderId(dto.getSenderId())
                .senderName(dto.getSenderName())
                .content(dto.getContent())
                .type(dto.getMessageType())
                .senderType(dto.getSenderType())
                .status(dto.getMessageStatus())
                .metadata(dto.getMetadata())
                .isRead(dto.getIsRead())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
