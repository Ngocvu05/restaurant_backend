package com.management.chat_service.service;

import com.management.chat_service.dto.GuestChatMessageDTO;

import java.util.List;

public interface IGuestChatService {
    void handleGuestMessage(GuestChatMessageDTO message);
    List<GuestChatMessageDTO> getMessages(String sessionId);
    void migrateToDatabase(String sessionId, Long userId);
}
