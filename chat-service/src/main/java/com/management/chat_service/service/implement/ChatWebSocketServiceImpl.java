package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.service.IChatWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWebSocketServiceImpl implements IChatWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    @Override
    public void sendMessageToRoom(String roomId, ChatMessageResponse message) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
}