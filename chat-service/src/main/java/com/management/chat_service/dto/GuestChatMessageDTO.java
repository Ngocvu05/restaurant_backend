package com.management.chat_service.dto;

import com.management.chat_service.status.MessageStatus;
import com.management.chat_service.status.MessageType;
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
public class GuestChatMessageDTO {
    private String sessionId;
    private String senderName;
    private String content;
    private MessageType messageType;
    private SenderType senderType;
    private MessageStatus messageStatus;
    private String metadata;
    private LocalDateTime createdAt;
}
