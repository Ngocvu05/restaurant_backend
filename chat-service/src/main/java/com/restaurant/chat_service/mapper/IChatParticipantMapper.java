package com.restaurant.chat_service.mapper;

import com.restaurant.chat_service.dto.ChatParticipantDto;
import com.restaurant.chat_service.model.ChatParticipant;
import com.restaurant.chat_service.model.ChatRoom;

public interface IChatParticipantMapper {
    ChatParticipantDto toDto(ChatParticipant participant);
    ChatParticipant toEntity(ChatParticipantDto dto, ChatRoom chatRoom);
}
