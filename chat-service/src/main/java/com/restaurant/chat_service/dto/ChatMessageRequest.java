package com.restaurant.chat_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String senderType;
    private String timestamp;
}
