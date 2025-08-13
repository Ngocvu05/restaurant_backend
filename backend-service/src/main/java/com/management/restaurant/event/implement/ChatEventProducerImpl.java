package com.management.restaurant.event.implement;

import com.management.restaurant.dto.SessionConversionEvent;
import com.management.restaurant.event.ChatEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatEventProducerImpl implements ChatEventProducer {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendSessionConversion(String sessionId, Long userId) {
        SessionConversionEvent event = new SessionConversionEvent();
        event.setSessionId(sessionId);
        event.setUserId(userId);
        log.info("ChatEventProducer - Sending session conversion event: {}", event);
        rabbitTemplate.convertAndSend("chat.exchange", "chat.routing.convert", event);
    }
}