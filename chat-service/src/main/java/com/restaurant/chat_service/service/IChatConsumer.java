package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageRequest;

public interface IChatConsumer {
    void receiveMessage(ChatMessageRequest request);
    void consumeMessage(ChatMessageRequest request);
}
