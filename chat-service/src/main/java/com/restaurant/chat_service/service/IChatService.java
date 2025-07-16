package com.restaurant.chat_service.service;

import com.restaurant.chat_service.dto.ChatMessageDTO;
import com.restaurant.chat_service.status.SenderType;

public interface IChatService {
    ChatMessageDTO processMessage(String roomId, Long senderId, String senderName, String content, SenderType senderType);
}
