package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;

public interface IChatProducerService {
    void sendToAI(String roomId, String content);
    void sendMessageToChatQueue(ChatMessageRequest request);
    void handleGuestAIMessage(ChatMessageRequest request);
}
