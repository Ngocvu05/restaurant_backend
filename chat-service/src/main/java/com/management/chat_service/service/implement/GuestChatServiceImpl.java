package com.management.chat_service.service.implement;

import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IGuestChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestChatServiceImpl implements IGuestChatService {
    private final IChatProducerService chatProducerService;
    @Override
    public String handleGuestMessage(GuestChatMessageDTO message) {
        return chatProducerService.handleGuestAIMessage(message.getSessionId(), message.getContent());
    }
}
