package com.management.chat_service.mapper;

import com.management.chat_service.dto.ChatParticipantDto;
import com.management.chat_service.model.ChatParticipant;
import com.management.chat_service.model.ChatRoom;

public interface IChatParticipantMapper {
    ChatParticipantDto toDto(ChatParticipant participant);
    ChatParticipant toEntity(ChatParticipantDto dto, ChatRoom chatRoom);
}
