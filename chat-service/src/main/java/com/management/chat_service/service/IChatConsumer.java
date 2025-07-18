package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;

public interface IChatConsumer {
    void receiveMessage(ChatMessageRequest request);
    void consumeMessage(ChatMessageRequest request);
}
