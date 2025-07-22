package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.SessionConversionEvent;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.service.IGuestChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class  SessionEventConsumer {
    private final IChatRoomService chatRoomService;
    private final IGuestChatService guestChatService;
    @RabbitListener(queues = RabbitMQConfig.CONVERT_SESSION_ROUTING_KEY)
    public void handleSessionConversion(SessionConversionEvent event) {
        chatRoomService.convertSessionToUser(event.getSessionId(), event.getUserId());
        log.info("✅ SessionEventConsumer - Converted session for ChatRoom{} to user {}", event.getSessionId(), event.getUserId());
        guestChatService.migrateToDatabase(event.getSessionId(), event.getUserId());
        log.info("✅ SessionEventConsumer - Converted session for ChatMessage {} to user {}", event.getSessionId(), event.getUserId());
    }
}
