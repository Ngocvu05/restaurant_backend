package com.management.chat_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.management.chat_service.status.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageRequest {
    private Long userId;
    private String chatRoomId;
    private String sessionId;
    private String message;
    private SenderType senderType;
    private LocalDateTime timestamp;
}