package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.dto.GuestChatMessageDTO;

import java.util.List;

public interface IGuestChatService {
    void handleGuestMessage(GuestChatMessageDTO message);
    void handleAIResponse(ChatMessageResponse response);
    List<Object> getGuestMessages(String sessionId);
    void migrateToDatabase(String sessionId, Long userId);
    void saveGuestMessageToRedis(ChatMessageRequest request);
    void saveGuestResponseToRedis(ChatMessageResponse response);
}
