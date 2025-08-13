package com.management.chat_service.dto;

import com.management.chat_service.status.ParticipantRole;
import com.management.chat_service.status.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantDto {
    private Long id;
    private Long userId;
    private String userName;
    private ParticipantRole role;
    private ParticipantStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime lastReadAt;
}