package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.service.IChatProducerService;
import com.management.chat_service.service.IGuestChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
    private final IChatProducerService chatProducerService;
    private final IGuestChatService guestChatService;

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(ChatMessageRequest request,
                                       @Header("simpSessionAttributes") Map<String, Object> sessionAttrs,
                                       @Header(name = "X-User-Id", required = false) Optional<String> userIdHeader) {
        log.info("📨 WebSocket message received: {}", request);
        String userId = (String) sessionAttrs.get("userId");

        // Sync userId from header if available
        if (userId != null) {
            request.setUserId(Long.parseLong(userId));
            chatProducerService.sendMessageToChatQueue(request);
            log.info("📨 WebSocketController - Send request via Websocket - Login message received: {}", request);
        }else{
            GuestChatMessageDTO message = GuestChatMessageDTO.builder()
                    .content(request.getMessage())
                    .sessionId(request.getSessionId())
                    .senderType(request.getSenderType())
                    .build();
            guestChatService.handleGuestMessage(message);
            log.info("📨 WebSocketController - Send request via Websocket - Guest message received: {}", request);
        }
    }
}
