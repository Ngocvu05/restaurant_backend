package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.model.ChatRoom;

import java.util.List;

public interface IChatRoomService {
    ChatRoom getOrCreateRoom(ChatMessageRequest request);
    void convertSessionToUser(String sessionId, Long userId);
    List<ChatRoom> getRooms(Long userId);
    List<ChatRoomDTO> getAllRooms(Long userId);
    List<ChatRoomDTO> getAllRoomsForAdmin();

    ChatRoom getOrCreatePrivateRoom(Long userId1, Long userId2);
}
