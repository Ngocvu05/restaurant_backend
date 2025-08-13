package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageResponse;

public interface IChatWebSocketService {
    void sendMessageToRoom(String roomId, ChatMessageResponse message);
    void sendMessageToPrivateRoom(String roomId, ChatMessageResponse message);
}