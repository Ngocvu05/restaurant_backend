package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageRequest;

public interface IChatProducerService {
    void sendToAI(String roomId, String content);
    void sendMessageToChatQueue(ChatMessageRequest request);
}
