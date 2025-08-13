package com.management.chat_service.dto;

import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String response;
    private String sessionId;
    private Long userId;
    private Long chatRoomId;
    private SenderType senderType;
    private MessageType messageType;
}