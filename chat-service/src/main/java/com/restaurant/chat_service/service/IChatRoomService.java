package com.restaurant.chat_service.service;

import com.restaurant.chat_service.model.ChatRoom;

public interface IChatRoomService {
    ChatRoom createNewRoom(Long userId);
}
