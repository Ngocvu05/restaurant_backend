package com.management.chat_service.mapper;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;

public interface IChatMessageMapper {
    ChatMessageDTO toDTO(ChatMessage message);
    ChatMessage toEntity(ChatMessageDTO dto, ChatRoom chatRoom);
}
