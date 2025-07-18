package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.model.ChatRoom;

import java.util.List;

public interface IChatRoomService {
    ChatRoom createNewRoom(Long userId);
    ChatRoom getOrCreateRoom(ChatMessageRequest request);
    void convertSessionToUser(String sessionId, Long userId);
    List<ChatRoom> getRooms(Long userId);
    List<ChatRoom> getAllRooms(Long userId);
}
