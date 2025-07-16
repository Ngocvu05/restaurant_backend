package com.restaurant.chat_service.mapper;

import com.restaurant.chat_service.dto.ChatMessageDTO;
import com.restaurant.chat_service.model.ChatMessage;
import com.restaurant.chat_service.model.ChatRoom;

public interface IChatMessageMapper {
    ChatMessageDTO toDTO(ChatMessage message);
    ChatMessage toEntity(ChatMessageDTO dto, ChatRoom chatRoom);
}
