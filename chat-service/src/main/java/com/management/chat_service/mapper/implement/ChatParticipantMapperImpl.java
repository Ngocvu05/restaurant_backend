package com.management.chat_service.mapper.implement;

import com.management.chat_service.dto.ChatParticipantDto;
import com.management.chat_service.mapper.IChatParticipantMapper;
import com.management.chat_service.model.ChatParticipant;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.status.ParticipantStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatParticipantMapperImpl implements IChatParticipantMapper {
    @Override
    public ChatParticipantDto toDto(ChatParticipant participant) {
        if (participant == null) return null;

        return ChatParticipantDto.builder()
                .id(participant.getId())
                .userId(participant.getUserId())
                .userName(participant.getUserName())
                .role(participant.getRole())
                .status(participant.getStatus())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .lastReadAt(participant.getLastReadAt())
                .build();
    }

    @Override
    public ChatParticipant toEntity(ChatParticipantDto dto, ChatRoom chatRoom) {
        if (dto == null || chatRoom == null) return null;

        return ChatParticipant.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .role(dto.getRole())
                .status(dto.getStatus() != null ? dto.getStatus() : ParticipantStatus.ACTIVE)
                .joinedAt(dto.getJoinedAt())
                .leftAt(dto.getLeftAt())
                .lastReadAt(dto.getLastReadAt())
                .chatRoom(chatRoom)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .build();
    }
}
