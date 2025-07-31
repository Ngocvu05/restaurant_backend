package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;

public interface IChatProducerService {
    void sendToAI(ChatMessageRequest request);
    void sendMessageToChatQueue(ChatMessageRequest request);
    void handleGuestAIMessage(ChatMessageRequest request);
    void sendMessageToUser(ChatMessageRequest request);

    void sendMessageToChatQueue_v2(ChatMessageRequest request);
}
