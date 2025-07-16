package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageResponse;

public interface IChatResponseConsumer {
    void receiveAIResponse(ChatMessageResponse response);
}
