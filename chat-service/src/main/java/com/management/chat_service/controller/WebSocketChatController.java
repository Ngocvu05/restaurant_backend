package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
    private final IChatProducerService chatProducerService;
    private final IGuestChatService guestChatService;

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(ChatMessageRequest request) {
        log.info("📨 WebSocket message received: {}", request);
        Long userId = request.getUserId();
        SenderType senderType = request.getSenderType();

        if (senderType == null) {
            senderType = userId != null ? SenderType.USER : SenderType.GUEST;
            request.setSenderType(senderType);
        }
        if (userId != null && senderType != SenderType.GUEST) {
            log.info("📨 WebSocketController - Send request via Websocket - Login message received: {}", request);
            chatProducerService.sendMessageToChatQueue(request);
        }else{
            GuestChatMessageDTO message = GuestChatMessageDTO.builder()
                    .content(request.getMessage())
                    .sessionId(request.getSessionId())
                    .senderType(request.getSenderType())
                    .build();
            log.info("📨 WebSocketController - Send request via Websocket - Guest message request: {} - message: {}", request, message);
            guestChatService.handleGuestMessage(message);
        }
    }
}
