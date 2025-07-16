package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageRequest;
import com.restaurant.chat_service.model.ChatRoom;

public interface IChatRoomService {
    ChatRoom createNewRoom(Long userId);
    ChatRoom getOrCreateRoom(ChatMessageRequest request);
    void convertSessionToUser(String sessionId, Long userId);
}
