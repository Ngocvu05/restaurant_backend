package com.management.chat_service.service;

import com.management.chat_service.dto.ChatMessageDTO;
import org.springframework.data.domain.Page;

public interface IChatMessageService {
    Page<ChatMessageDTO> getMessagesByRoomId(String roomId, int page, int size);
}
