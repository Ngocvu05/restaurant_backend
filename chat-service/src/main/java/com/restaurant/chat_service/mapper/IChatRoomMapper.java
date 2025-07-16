package com.restaurant.chat_service.mapper;

import com.restaurant.chat_service.dto.ChatRoomDTO;
import com.restaurant.chat_service.model.ChatRoom;

public interface IChatRoomMapper {
    ChatRoomDTO toDTO(ChatRoom chatRoom);
    ChatRoom toEntity(ChatRoomDTO dto);
}
