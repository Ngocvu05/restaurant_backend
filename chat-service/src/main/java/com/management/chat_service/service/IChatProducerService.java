package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;

public interface IChatProducerService {
    void sendToAI(String roomId, String content);
    void sendMessageToChatQueue(ChatMessageRequest request);
    String handleGuestAIMessage(String sessionId, String content);
}
