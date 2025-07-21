package com.management.chat_service.consumer;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.SessionConversionEvent;
import com.management.chat_service.service.IChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventConsumer {
    private final IChatRoomService chatRoomService;

    @RabbitListener(queues = RabbitMQConfig.CONVERT_SESSION_ROUTING_KEY)
    public void handleSessionConversion(SessionConversionEvent event) {
        log.info("📥 SessionEventConsumer - Get event session → user: {}", event);
        chatRoomService.convertSessionToUser(event.getSessionId(), event.getUserId());
        log.info("✅ SessionEventConsumer - Converted session {} to user {}", event.getSessionId(), event.getUserId());
    }
}
