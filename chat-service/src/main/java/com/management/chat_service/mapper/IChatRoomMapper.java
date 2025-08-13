package com.management.chat_service.mapper;

import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.model.ChatRoom;

public interface IChatRoomMapper {
    ChatRoomDTO toDTO(ChatRoom chatRoom);
    ChatRoom toEntity(ChatRoomDTO dto);
}