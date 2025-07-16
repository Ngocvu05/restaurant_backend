package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageResponse;

public interface IChatWebSocketService {
    void sendMessageToRoom(String roomId, ChatMessageResponse message);
}
