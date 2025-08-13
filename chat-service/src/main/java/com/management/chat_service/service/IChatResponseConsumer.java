package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageResponse;

public interface IChatResponseConsumer {
    void receiveAIResponse(ChatMessageResponse response);
}