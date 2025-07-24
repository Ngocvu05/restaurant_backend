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
    @RabbitListener(queues = RabbitMQConfig.SESSION_CONVERT_QUEUE)
    public void handleSessionConversion(SessionConversionEvent event) {
        if (event == null || event.getSessionId() == null || event.getUserId() == null) {
            log.warn("⚠️ SessionEventConsumer - Nhận event không hợp lệ: {}", event);
            return;
        }

        guestChatService.migrateToDatabase(event.getSessionId(), event.getUserId());
        log.info("✅ SessionEventConsumer - migrate message Redis -> DB cho session Room {} -> User {}", event.getSessionId(), event.getUserId());
    }
}
