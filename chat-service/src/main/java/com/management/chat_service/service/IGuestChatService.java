package com.management.chat_service.service;

import com.management.chat_service.dto.GuestChatMessageDTO;

public interface IGuestChatService {
    String handleGuestMessage(GuestChatMessageDTO message);
}
