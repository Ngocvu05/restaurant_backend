package com.management.search_service.config;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message message, Channel channel, org.springframework.messaging.Message<?> message1, ListenerExecutionFailedException exception) throws Exception {
        log.error("Error in message processing", exception);

        // Log message details
        if (message1 != null) {
            message1.getPayload();
            log.error("Failed message payload: {}", message1.getPayload().toString());
        }

        // Check if this is a poison message (failed multiple times)
        Integer deliveryCount = (Integer) message.getMessageProperties()
                .getHeaders().get("x-delivery-count");

        if (deliveryCount != null && deliveryCount >= 3) {
            log.error("Message failed {} times, sending to DLQ", deliveryCount);
            throw new AmqpRejectAndDontRequeueException("Message failed multiple times", exception);
        }

        throw exception;
    }
}